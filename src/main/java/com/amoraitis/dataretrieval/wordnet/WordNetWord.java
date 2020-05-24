package com.amoraitis.dataretrieval.wordnet;

public class WordNetWord {
    private long synset_id;
    private W_NUM w_num;
    private String word;
    private SS_TYPE ss_type;
    private int sense_number;
    private int tag_count;
    private String actualSynset;

    public WordNetWord(long synset_id, int w_num, String word, String ss_type, int sense_number, int tag_count, String actualSynset) {
        this.synset_id = synset_id;
        this.w_num = getEnum(w_num);
        this.word = word;
        this.ss_type = getEnum(ss_type);
        this.sense_number = sense_number;
        this.tag_count = tag_count;
        this.actualSynset = actualSynset;
    }

    public SS_TYPE getSs_type() {
        return ss_type;
    }

    public String getActualSynset() {
        return actualSynset;
    }

    public enum W_NUM {
        UNKNOWN,
        NOUN,
        VERB,
        ADJECTIVE,
        ADVERB
    }

    public enum SS_TYPE{
        UNKNOWN,
        NOUN,
        VERB,
        ADJECTIVE,
        ADJECTIVE_SATELLITE,
        ADVERB
    }

    private W_NUM getEnum(int number) {
        switch (number) {
            case 1:
                return W_NUM.NOUN;
            case 2:
                return W_NUM.VERB;
            case 3:
                return W_NUM.ADJECTIVE;
            case 4:
                return W_NUM.ADVERB;
            default:
                return W_NUM.UNKNOWN;
        }
    }

    private SS_TYPE getEnum(String character) {
        switch (character) {
            case "n":
                return SS_TYPE.NOUN;
            case "v":
                return SS_TYPE.VERB;
            case "a":
                return SS_TYPE.ADJECTIVE;
            case "s":
                return SS_TYPE.ADJECTIVE_SATELLITE;
            case "r":
                return SS_TYPE.ADVERB;
            default:
                return SS_TYPE.UNKNOWN;
        }
    }
}
