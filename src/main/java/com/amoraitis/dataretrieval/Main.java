/**
 * @author amoraitis
 */

package com.amoraitis.dataretrieval;

import com.amoraitis.dataretrieval.serializers.InputSerializer;

public class Main {

    public static void main(String[] args) {
        InputSerializer serializer = new InputSerializer();
        serializer.loadData();
    }
}
