/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval.serializers;

import com.amoraitis.dataretrieval.model.QueryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.amoraitis.dataretrieval.mappers.Mapper.QueryResponseMapper.mapResponse;

public class QueriesOutputDeserializer {

    private List<QueryResponse> responses;

    public QueriesOutputDeserializer(Map<String, String> responses){
        this.responses = new ArrayList<>();
        this.loadResponses(responses);
    }

    private void loadResponses(Map<String, String> responses) {
        this.responses = responses
                .entrySet()
                .stream()
                .map((pair)-> {return mapResponse(pair.getKey(), pair.getValue());})
                .collect(Collectors.toList());
    }

    public List<QueryResponse> getResponses(){
        return responses;
    }
}