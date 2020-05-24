package com.amoraitis.dataretrieval.serializers;

import com.amoraitis.dataretrieval.model.QueryResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TrecEvalSerializer {
    private Map<Integer, List<QueryResponse>> mappedResponses;

    public TrecEvalSerializer(List<QueryResponse> responses) {
        this.initializeMap(responses);
    }

    private List<QueryResponse> getForK(List<QueryResponse> responses, int k) {
        return responses
                .stream()
                .filter(r -> r.getK() == k)
                .sorted(Comparator.comparingInt(QueryResponse::getQueryId))
                .collect(Collectors.toList());
    }

    private void initializeMap(List<QueryResponse> responses) {
        this.mappedResponses = new HashMap<>();
        mappedResponses.put(20, this.getForK(responses, 20));
        mappedResponses.put(30, this.getForK(responses, 30));
        mappedResponses.put(50, this.getForK(responses, 50));
    }

    public Map<Integer, String> generateTrecEvalInputs(String dataFolder) {
        Map<Integer, String> paths = new HashMap<>();
        this.mappedResponses.forEach((k, v) -> {
            String path = dataFolder + File.separator + "qrels" + k + ".txt";
            paths.put(k, path);

            try (FileWriter fileWriter = new FileWriter(path, false)) {
                v.forEach(r -> {
                    String formattedQueryId = String.format("Q%02d", r.getQueryId());
                    r.getDocs().forEach((code, score) -> {
                        try {
                            fileWriter
                                    .append(formattedQueryId)
                                    .append(" Q0 ")
                                    .append(String.valueOf(code))
                                    .append(" 1 ")
                                    .append(String.valueOf(score))
                                    .append(" STANDARD\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                });
                fileWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        return paths;
    }
}
