/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval;

import com.amoraitis.dataretrieval.model.QueryResponse;
import com.amoraitis.dataretrieval.serializers.InputDeserializer;
import com.amoraitis.dataretrieval.serializers.InputSerializer;
import com.amoraitis.dataretrieval.serializers.QueriesOutputDeserializer;
import com.amoraitis.dataretrieval.serializers.TrecEvalSerializer;
import com.amoraitis.dataretrieval.services.QueriesService;
import com.amoraitis.dataretrieval.wordnet.WordNetSerializer;
import com.amoraitis.dataretrieval.wordnet.WordNetWord;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.sun.xml.internal.ws.developer.Serialization;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.toilelibre.libe.curl.Curl.curl;

public class Main {
    private final static File currentFilePath = new File(InputSerializer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private static String dataFolder = currentFilePath.getParentFile().getParentFile()+ File.separator+"data"+File.separator;
    private static String documentsSource = dataFolder + File.separator + "documents.txt";
    private static String queriesSource = dataFolder + File.separator + "queries.txt";
    private static String queries = dataFolder + File.separator + "queries.json";
    private static String qrels = dataFolder + File.separator + "qrels.txt";
    private static String wordnetFile = dataFolder + File.separator + "wn_s.pl";
    private static String index = dataFolder + String.valueOf('\\') + "index.json";
    private static String wordnetJSONTemplate = dataFolder + String.valueOf('\\') + "put-wordnet.json.template";
    private static String path_to_treceval;

    public static void main(String[] args) throws IOException, HttpException {
        InputDeserializer documentsDeserializer = new InputDeserializer();
        documentsDeserializer.loadData(documentsSource);

        InputSerializer serializer = new InputSerializer(queries);
        serializer.initializeData(documentsDeserializer.getDocuments());
        serializer.serializeJSON();

        // Delete index if exists
        HttpResponse deleteResponse = curl("-XDELETE \"http://localhost:9200/data-retrieval?pretty\"");
        System.out.println(deleteResponse.getStatusLine().getStatusCode() + "\t" + deleteResponse.getStatusLine().getReasonPhrase());
        index = index.replace("\\\\", "\\").replace("\\","/");

        // Create index
        HttpResponse createResponse = curl("-XPUT \"http://localhost:9200/data-retrieval?pretty\" -H 'Content-Type: application/json' --data-binary \'@"
                + index + "\'");
        System.out.println(createResponse.getStatusLine().getStatusCode() + "\t" + createResponse.getStatusLine().getReasonPhrase());

        // Post docs
        postDocs();

        // Deserialize given queries
        InputDeserializer queriesDeserializer = new InputDeserializer();
        queriesDeserializer.loadData(queriesSource);

        // Run queries
        QueriesService queriesService = new QueriesService(queriesDeserializer.getDocuments(), dataFolder + File.separator + "phase1");
        Map<String, String> responses = queriesService.executeRequests();

        QueriesOutputDeserializer queriesOutputDeserializer = new QueriesOutputDeserializer(responses);
        List<QueryResponse> queryResponses = queriesOutputDeserializer.getResponses();

        TrecEvalSerializer trecEvalSerializer = new TrecEvalSerializer(queryResponses);
        Map<Integer, String> inputs = trecEvalSerializer.generateTrecEvalInputs(dataFolder + File.separator + "phase1");

        try{
            path_to_treceval = args[0];
            runTrecEvalCommand(inputs);
        }catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
            System.err.println("Path to trec-eval must be given as a parameter!");
        }

        // Phase 2
        // Deserialize wn_s.pl - left out due to errors parsing the .pl file. Original goal was to filter the categories of the synsets.
        //WordNetSerializer wordNetSerializer = new WordNetSerializer(wordnetFile);
        // filter only nouns and adverbs
        /*List<WordNetWord> synsets = wordNetSerializer.getSynonyms()
                .stream()
                .filter(s -> s.getSs_type().equals(WordNetWord.SS_TYPE.NOUN) || s.getSs_type().equals(WordNetWord.SS_TYPE.ADVERB))
                .collect(Collectors.toList());*/

        // Create index-settings json with the synonyms
        //Gson gson = new Gson();
        //String synsetsAsJSON = gson.toJson(synsets.stream().map(WordNetWord::getActualSynset).collect(Collectors.toList()));
        String worndnetPutJsonPayload = String.join("", Files.readAllLines(Paths.get(wordnetJSONTemplate), StandardCharsets.UTF_8));
        worndnetPutJsonPayload = worndnetPutJsonPayload.replace("@wordnet-path", wordnetFile);
        String wordnetPutPayloadPath = dataFolder + File.separator + "put-wordnet.json";
        FileWriter writer = new FileWriter(wordnetPutPayloadPath, false);
        writer.write(worndnetPutJsonPayload);
        writer.flush();
        writer.close();

        // PUT extended index settings
        HttpResponse extendQueriesResponse = curl("-XPUT \"http://localhost:9200/data-retrieval\" -H 'Content-Type: application/json' --data-binary \'@"
                + normalizePath(wordnetPutPayloadPath));

        // Run queries
        QueriesService queriesService2 = new QueriesService(queriesDeserializer.getDocuments(), dataFolder + File.separator + "phase2");
        Map<String, String> responses2 = queriesService2.executeRequests();

        QueriesOutputDeserializer queriesOutputDeserializer2 = new QueriesOutputDeserializer(responses2);
        List<QueryResponse> queryResponses2 = queriesOutputDeserializer2.getResponses();

        TrecEvalSerializer trecEvalSerializer2 = new TrecEvalSerializer(queryResponses2);
        Map<Integer, String> inputs2 = trecEvalSerializer.generateTrecEvalInputs(dataFolder + File.separator + "phase2");

        try{
            path_to_treceval = args[0];
            runTrecEvalCommand(inputs2);
        }catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
            System.err.println("Path to trec-eval must be given as a parameter!");
        }

    }

    private static void runTrecEvalCommand(Map<Integer, String> inputs) {
        Runtime rt = Runtime.getRuntime();
        inputs.forEach((key, value) -> {
            try {
                Process mapAvg = rt.exec("cmd.exe /c " + normalizePath(path_to_treceval) + " -m map " + qrels + " " + normalizePath(value) + " > " + normalizePath(value.replace("qrels", "trecrels")));
                Arrays.asList(5, 10 , 15, 20).forEach(k ->{
                    try {
                        Process avgFirstK = rt.exec("cmd.exe /c " + normalizePath(path_to_treceval) + " -m P_" + k + " " + qrels + " " + normalizePath(value) + " > " + normalizePath(value.replace(".txt", "k"+k+".txt").replace("qrels", "trecrels")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                //System.out.println(new BufferedReader(new InputStreamReader(pr.getInputStream()))
                //        .lines().collect(Collectors.joining("\n")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private static String normalizePath(String path){
        return path.replace("\\\\", "\\").replace("\\","/");
    }

    private static void postDocs() throws HttpException {
         HttpResponse response = curl("-XPOST \"http://localhost:9200/data-retrieval/_doc/_bulk?pretty\" -H 'Content-Type: application/json' --data-binary \'@"
                 + normalizePath(queries) +"\'");
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
