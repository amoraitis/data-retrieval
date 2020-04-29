package com.amoraitis.dataretrieval.services;

import com.amoraitis.dataretrieval.model.Document;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.AbstractMap.SimpleEntry;
import static org.toilelibre.libe.curl.Curl.curl;

public class QueriesService {
    private final List<Document> queries;
    private String pathToData;

    public QueriesService(List<Document> queries, String pathToData) {
        this.queries = queries;
        this.pathToData = pathToData;
    }

    private String getSingleQuery(String queryString, int k) {
        return "-XGET \"http://localhost:9200/data-retrieval/_search\" -H 'Content-Type: application/json' -d'{\"from\":0, \"size\":" + k + ",\"query\":{\"query_string\":{\"query\":\"" + queryString + "\"}}}'";
    }

    private Callable<HttpResponse> executeSingleRequest(String queryString, int k) {
        return () -> curl(getSingleQuery(queryString, k));
    }

    public List<String> executeRequests() {
        Map<String, Callable<HttpResponse>> requests = new HashMap<>();
        Arrays.asList(20,30,50).forEach( k ->
                queries.forEach(doc ->
                        requests.put(String.format("q%d_k%d", doc.getCode(), k), executeSingleRequest(doc.getText(), k)))
        );

        ExecutorService queriesService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

            List<AbstractMap.Entry<String, HttpResponse>> executedRequests = requests.entrySet().stream().map((pair) -> {
                queriesService.submit(pair.getValue());

                try {
                    HttpResponse response = pair.getValue().call();

                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new HttpException("Couldn't get query...");
                    } else {
                        return new SimpleEntry<>(pair.getKey(), response);
                    }
                } catch (HttpException httpEx) {
                    System.err.println(httpEx.getMessage());
                    throw new IllegalStateException(httpEx.getMessage());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).collect(Collectors.toList());

            // Save off responses
            // @Warning: Only for reference
            executedRequests.forEach(pair ->{
                try {
                    HttpResponse httpResponse = pair.getValue();
                    String response = EntityUtils.toString(httpResponse.getEntity());

                    Gson gson = new GsonBuilder().create();
                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                    FileWriter writer = new FileWriter(pathToData+ File.pathSeparator +"temp" + File.pathSeparator + pair.getKey() + ".json");
                    gson.toJson(jsonResponse, writer);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            return executedRequests.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }
}

