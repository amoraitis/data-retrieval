/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Document {
    private int code;
    private List<String> answers;

    public Document() {
        answers = new ArrayList<>();
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

    private List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public void addAnswer(String string){
        answers.add(string);
    }

    @Override
    public String toString() {
        return String.format(
                "{\"tags\":[\"docs\"], \"code\":%s, \"answers\":[%s]}",
                code,
                this.getAnswers().parallelStream()
                        .map(answer -> String.format("{\"answer\": \"%s\"}", answer.replace("\"","\\\"")))
                        .collect(Collectors.joining(","))

        );
    }
}
