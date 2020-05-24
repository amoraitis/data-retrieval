Φάση 2
Anastasios Moraitis - p3140138

Προστέθηκε κώδικας που κάνει extend με συνώνυμα το index διενεργεί ξάνα queries και τρέχει με το trec eval τις συγκρισεις.
Παρακάτω είναι ένα snippet από τη Main.java.

Αφού τρέξετε το πρόγραμμα θα βρείτε output για results και τα αποτελέσματα από trec eval.

Τα trecrels{k}k{5} για το avg στα k πρώτα results δεν αναγνριζεται ως εντολή και ως εκτούτου τα αρχεία αυτά έχουν empty output.


```
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
```