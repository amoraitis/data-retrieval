package com.amoraitis.dataretrieval.wordnet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WordNetSerializer {
    private List<WordNetWord> synonyms;

    public WordNetSerializer(String wordnetFile){
        this.synonyms = new ArrayList<>();
        this.loadSynonyms(wordnetFile);
    }

    public List<WordNetWord> getSynonyms() {
        return synonyms;
    }

    private void loadSynonyms(String path){
        try (Scanner reader = new Scanner(new File(path))) {
            while (reader.hasNextLine()){
                String synonym = reader.nextLine();

                if(!synonym.isEmpty()){
                    String actualSynset = synonym;
                    synonym = synonym.replace("s(", "");
                    synonym = synonym.replace(").", "");
                    String[] synonymParts = synonym.split(",");
                    this.synonyms.add(
                            new WordNetWord(Integer.parseInt(synonymParts[0]),
                                    Integer.parseInt(synonymParts[1]),
                                    synonymParts[2],
                                    synonymParts[3],
                                    Integer.parseInt(synonymParts[4]),
                                    Integer.parseInt(synonymParts[5]),
                                    actualSynset));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}