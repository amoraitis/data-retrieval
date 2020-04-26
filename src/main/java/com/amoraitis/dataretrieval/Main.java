/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval;

import com.amoraitis.dataretrieval.serializers.InputDeserializer;
import com.amoraitis.dataretrieval.serializers.InputSerializer;

import java.io.File;
import java.io.IOException;

import static org.toilelibre.libe.curl.Curl.curl;

public class Main {
    private final static File currentFilePath = new File(InputSerializer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private static String dataFolder = currentFilePath.getParentFile().getParentFile()+ File.separator+"data"+File.separator;
    private static String source = dataFolder + File.separator + "documents.txt";
    private static String queries = dataFolder + File.separator + "queries.json";

    public static void main(String[] args) throws IOException {
        InputDeserializer deserializer = new InputDeserializer();
        deserializer.loadData(source);

        InputSerializer serializer = new InputSerializer(queries);
        serializer.initializeData(deserializer.getDocuments());
        serializer.serializeJSON();

        postDocs();
    }

    private static void postDocs(){
        curl("-XPOST \"http://localhost:9200/data-retrieval/doc/_bulk?pretty\" -H 'Content-Type: application/json' --data-binary @" + queries);
    }
}
