package com.amoraitis.dataretrieval.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryResponse {
    private Map<Integer, Double> docsIdsScores;
    private int queryId;
    private int k;

    public QueryResponse(int k, int queryId) {
        this.queryId = queryId;
        this.k = k;
        this.docsIdsScores = new HashMap<>();
    }

    public Map<Integer, Double> getDocs(){
        return this.docsIdsScores
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new));
    }

    public void addDocument(int documentId, double score){
        this.docsIdsScores.put(documentId, score);
    }

    public int getQueryId() {
        return queryId;
    }

    public int getK() {
        return k;
    }
}
