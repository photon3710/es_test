package com.qd.indexing;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;

import com.qd.util.iters.*;
import com.qd.util.*;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Introduction: Entrance to index from external files in batch.
 * 
 * Usage: 
 * 1. make sure you are using java 1.8
 * 2. after compiling files, cd indexing/
 * 3. java -cp build/libs/indexing-0.1.jar com.qd.indexing.IndexingDriver
 * 
 */


public class IndexingDriver {
    private static final String uixPath = "/Users/swang/Documents/elasticsearch/ash_es/data/pathToUix.json";
    private static final int batchSize = 8;

    public static void main(String[] args) throws IOException{
        IndexManager indexManager = new IndexManager();
        Node node = null;

        node = indexManager.init(new Properties(), "");


        JsonParser parser = new JsonParser();
        Object obj = parser.parse(new FileReader(uixPath));

        JsonArray jsonArray = (JsonArray) obj;
        JsonObject[] anArray;
        anArray = new JsonObject[jsonArray.size()];
        
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObj = (JsonObject) jsonArray.get(i);
            anArray[i] = jsonObj;
        }
        
        ArrayIterable<JsonObject> uixStream = new ArrayIterable<JsonObject>(anArray);
                    
        
        Function<JsonObject, String> idExtractor = json -> {
            String res = null;
            try {
                res = URLEncoder.encode(json.get("uixFileName").toString(), "utf-8");
            } catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
            }
            return res;
        };

        Function<JsonObject, String> func = json -> {
            return json.toString();
        };

        final QdBatchIndexer<JsonObject> batchIndexer = new QdBatchIndexer<>(node, func, idExtractor);
        batchIndexer.setIndexName(indexManager.indexName);
        batchIndexer.setTypeName(indexManager.typeName);
        batchIndexer.index(uixStream);

        indexManager.close();
    }
}
