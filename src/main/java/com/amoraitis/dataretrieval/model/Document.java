/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval.model;

public class Document {
    private int code;
    private String text;

    public Document() {}

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format(
                "{\"tags\":[\"docs\"], \"code\":%s, \"text\": \"%s\" }",
                code,
                this.getText().replace("\"","\\\""));
    }
}
