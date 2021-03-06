/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval.serializers;

import com.amoraitis.dataretrieval.model.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputDeserializer {
    private List<Document> documents = new ArrayList<Document>();

    public void loadData(String source){

        try {
            File input = new File(source);
            Scanner myReader = new Scanner(input);

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Document currentDoc = new Document();
                StringBuilder text = new StringBuilder();
                currentDoc.setCode(Integer.parseInt(data.trim()));
                data = myReader.nextLine();

                while(!data.trim().equals("///") && myReader.hasNextLine()){
                    text.append(data.trim());

                    try{
                        data = myReader.nextLine();
                    }catch(java.util.NoSuchElementException e){
                        e.printStackTrace();
                    }
                }

                currentDoc.setText(text.toString());
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
