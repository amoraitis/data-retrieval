package com.amoraitis.dataretrieval.services;

import com.amoraitis.dataretrieval.model.Document;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
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
        return "-XPOST \"http://localhost:9200/data-retrieval/_search\" -H 'Content-Type: application/json' -d'{\"from\":1,\"size\":" + k + ",\"query\":{\"query_string\":{\"query\":\"" + queryString + "\"}}}'";
    }

    private Callable<HttpResponse> executeSingleRequest(String queryString, int k) {
        return () -> curl(getSingleQuery(queryString, k));
    }

    public Map<String, String> executeRequests() {
        Map<String, String> result = new HashMap<>();
        Map<String, Callable<HttpResponse>> requests = new HashMap<>();
        Arrays.asList(20, 30, 50).forEach( k ->
                queries.forEach(doc ->
                        requests.put(String.format("q%d_k%d", doc.getCode(), k), executeSingleRequest(doc.getText(), k)))
        );

        ExecutorService queriesService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

        List<AbstractMap.Entry<String, Future<HttpResponse>>> executedRequests = requests.entrySet().stream()
                .map((pair) -> new SimpleEntry<>(pair.getKey(), queriesService.submit(pair.getValue()))).collect(Collectors.toList());

        queriesService.shutdown();

        Path pathToTemp = Paths.get(pathToData + "//temp//temp");
        try {
            Files.createDirectories(pathToTemp.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save off responses
        // @Warning: Only for reference(... and trec-eval)
        executedRequests.forEach(pair ->{
            try {
                HttpResponse httpResponse = pair.getValue().get();

                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    throw new HttpException("Couldn't get query...");
                }

                String response = EntityUtils.toString(httpResponse.getEntity());
                result.put(pair.getKey(), response);

                FileWriter writer = new FileWriter(pathToData+ "//temp//" + pair.getKey() + ".json");
                writer.write(response);
                writer.flush();
                writer.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } catch (HttpException httpEx) {
                httpEx.printStackTrace();
                System.err.println(httpEx.getMessage());
                throw new IllegalStateException(httpEx.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        return result;
    }
}

