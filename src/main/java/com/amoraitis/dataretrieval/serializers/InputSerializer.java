/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval.serializers;

import com.amoraitis.dataretrieval.model.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputSerializer {
    private final static File currentFilePath = new File(InputSerializer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private static String dataFolder = currentFilePath.getParentFile().getParentFile()+ File.separator+"data"+File.separator;

    private List<Document> documents = new ArrayList<Document>();

    public void loadData(){

        try {
            File input = new File(dataFolder + File.separator + "documents.txt");
            Scanner myReader = new Scanner(input);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Document currentDoc = new Document();
                String answer = "";
                currentDoc.setCode(Integer.parseInt(data.trim()));
                data = myReader.nextLine();

                while(!data.trim().equals("///") && myReader.hasNextLine()){
                    if(!data.trim().isEmpty()){
                        answer += data.trim();
                    }else{
                        currentDoc.addAnswer(answer);
                        answer = "";
                    }

                    try{
                        data = myReader.nextLine();
                    }catch(java.util.NoSuchElementException e){
                        e.printStackTrace();
                    }
                }

                currentDoc.addAnswer(answer);
                this.addDocument(currentDoc);
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public void addDocument(Document document){
        documents.add(document);
    }
}
