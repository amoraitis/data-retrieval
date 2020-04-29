/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval.serializers;

import com.amoraitis.dataretrieval.model.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputSerializer {

    private List<Document> data = new ArrayList<>();
    private String queries;

    public InputSerializer(String queries) {
        this.queries = queries;
    }

    public void initializeData(List<Document> documents){
        data = documents;
    }

    public void serializeJSON() throws IOException {
        FileWriter queriesFile = new FileWriter(queries, false);

        for (int i = 0; i < data.size(); i++) {
            Document doc = data.get(i);
            try {
                String index = String.format("{\"index\":{\"_id\":\"%s\"}}", i + 1);
                queriesFile.append(index);
                queriesFile.append(System.lineSeparator());
                queriesFile.append(doc.toString());
                queriesFile.append(System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        queriesFile.flush();
        queriesFile.close();
    }
}
