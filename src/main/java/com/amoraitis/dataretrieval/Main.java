/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval;

import com.amoraitis.dataretrieval.serializers.InputDeserializer;
import com.amoraitis.dataretrieval.serializers.InputSerializer;
import com.amoraitis.dataretrieval.services.QueriesService;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.toilelibre.libe.curl.Curl.curl;

public class Main {
    private final static File currentFilePath = new File(InputSerializer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private static String dataFolder = currentFilePath.getParentFile().getParentFile()+ File.separator+"data"+File.separator;
    private static String documentsSource = dataFolder + File.separator + "documents.txt";
    private static String queriesSource = dataFolder + File.separator + "queries.txt";
    private static String queries = dataFolder + File.separator + "queries.json";
    private static String index = dataFolder + String.valueOf('\\') + "index.json";

    public static void main(String[] args) throws IOException, HttpException {
        InputDeserializer documentsDeserializer = new InputDeserializer();
        documentsDeserializer.loadData(documentsSource);

        InputSerializer serializer = new InputSerializer(queries);
        serializer.initializeData(documentsDeserializer.getDocuments());
        serializer.serializeJSON();

        // Delete index if exists
        //HttpResponse deleteResponse = curl("-XDELETE \"http://localhost:9200/data-retrieval?pretty\"");
        index = index.replace("\\\\", "\\").replace("\\","/");
        // Create index
        //HttpResponse createResponse = curl("-XPUT \"http://localhost:9200/data-retrieval?pretty\" -H 'Content-Type: application/json' --data-binary \'@"
         //       + index + "\'");

        // Post docs
        //postDocs();

        // Deserialize given queries
        InputDeserializer queriesDeserializer = new InputDeserializer();
        queriesDeserializer.loadData(queriesSource);

        // Run queries
        QueriesService queriesService = new QueriesService(queriesDeserializer.getDocuments(), dataFolder);
        List<String> responses = queriesService.executeRequests();
    }

    private static String normalizePath(String path){
        return path.replace("\\\\", "\\").replace("\\","/");
    }

    private static void postDocs() throws HttpException {
         HttpResponse response = curl("-XPOST \"http://localhost:9200/data-retrieval/_doc/_bulk?pretty\" -H 'Content-Type: application/json' --data-binary \'@" + normalizePath(queries) +"\'");
         int responseCode = response.getStatusLine().getStatusCode();

         if(responseCode == 200 || responseCode == 201){
             System.out.println("Docs posted successfully!");
         }
         else {
             System.err.println("Couldn't post docs!");
             throw new HttpException("Couldn't post docs, cannot continue execution!");
         }
    }
}
