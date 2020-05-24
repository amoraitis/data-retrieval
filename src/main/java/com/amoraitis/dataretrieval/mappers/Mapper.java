package com.amoraitis.dataretrieval.mappers;

import com.amoraitis.dataretrieval.model.QueryResponse;
import com.google.gson.*;

public class Mapper {
    public static class QueryResponseMapper{
        public static QueryResponse mapResponse(String key, String response){
            int k = Integer.parseInt(key.split("_")[1].substring(1));
            int queryId = Integer.parseInt(key.split("_")[0].substring(1));
            QueryResponse result = new QueryResponse(k, queryId);

            JsonParser parser = new JsonParser();
            JsonElement rootElement = parser.parse(response);
            JsonArray docs = rootElement.getAsJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");

            for (JsonElement element:docs) {
                JsonObject doc = element.getAsJsonObject();
                result.addDocument(
                        doc.get("_source").getAsJsonObject().get("code").getAsInt(),
                        doc.get("_score").getAsDouble());
            }

            return result;
        }
    }
}
