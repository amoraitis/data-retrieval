/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval.model;

import java.util.ArrayList;
import java.util.List;

public class Document {
    private int code;
    private List answers;

    public Document() {
        answers = new ArrayList();
    }

    public Document(int code, List<String> answers) {
        this.code = code;
        this.answers = answers;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public void addAnswer(String string){
        answers.add(string);
    }
}
