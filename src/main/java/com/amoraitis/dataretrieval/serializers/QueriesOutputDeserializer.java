/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class QueriesOutputDeserializer {

    private List<String> responses;

    public class DocumentInfo{
        public String query;
        public int code;
        public float score;

        public DocumentInfo(String query, int code, float score){
            this.query = query;
            this.code = code;
            this.score = score;
        }
    }

    public QueriesOutputDeserializer(List<String> responses){
        this.responses = responses;
    }

    public void loadData(String pathToData){

        this.responses.forEach(response ->{
            Gson gson = new GsonBuilder().create();
            FileReader reader = null;
            try {
                reader = new FileReader(pathToData + File.pathSeparator +"temp" + File.pathSeparator + response + ".json");
                JsonObject jsonResponse = gson.fromJson(reader, JsonObject.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

    }


}