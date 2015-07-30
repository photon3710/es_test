package com.jidian.indexing;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.jidian.util.BoundedForkJoinPool;
import com.jidian.util.GsonConverter;
import com.jidian.util.iters.LineIterable;
import com.jidian.util.iters.ArrayIterable;
import com.jidian.util.InputStreamBuilder;
import com.jidian.util.ValueAggregator;
import com.jidian.util.iters.ChunkIterable;
import com.jidian.util.iters.FilteringIterable;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Entry point for command-line build raw app directly from file.
 * 
 * To make sure that you have java 1.8,
 * export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
 * 
 * Then under indexing:
 * java -cp build/libs/indexing-0.1.jar com.jidian.indexing.RawAppIndexingDriver
 */

public class RawAppIndexingDriver {
    private static final String rawAppProperties = "/raw_app_indexing.properties";
    private static final String activityProperties = "/activity_indexing.properties";
    private static final String uixPath = "/Users/swang/Documents/elasticsearch/ash_es/data/pathToUix.json";

    private static boolean dumpOut = false;
    private static boolean handleApp = false;
    private static boolean handleActivity = false;
    private static boolean handleUix = true;
    
    private static final int batchSize = 512;

    public static InputStreamReader buildStream(String propertyFile) throws IOException {
        Properties property = new Properties();
        property.load(RawAppIndexingDriver.class.getResourceAsStream(propertyFile));
        InputStreamBuilder builder = InputStreamBuilder.getBuilder(property, "");
        InputStream inputStream = builder.buildInputStream();
        return new InputStreamReader(inputStream, "utf-8");
    }


    public static void main(String[] args) throws IOException {
        Logger LOGGER = LoggerFactory.getLogger(RawAppIndexingDriver.class);


        IndexManager indexManager = new IndexManager();
        Node node = null;
        if (!dumpOut) {
            node = indexManager.init(new Properties(), "");
        }

        // We need to take care of the activity search now,
        if (handleActivity) {

            InputStreamReader inputReader = buildStream(activityProperties);
            JsonParser parser = new JsonParser();
            Function<String, JsonObject> converter = txt -> { return (JsonObject)parser.parse(txt); };
            LineIterable<JsonObject> avroStream = new LineIterable<JsonObject>(inputReader, converter);
            if (dumpOut) {
                BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/tmpa.txt"));
                for (JsonObject app : avroStream) {
                    writer.write(app.toString());
                    writer.newLine();
                }
                writer.close();
            } else {
                Function<JsonObject, String> idExtractor = json -> {
                    String res = null;
                    try {
                        res = URLEncoder.encode(json.get("intent").toString(), "utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        uee.printStackTrace();
                    }
                    return res;
                };

                Function<JsonObject, String> func = json -> {
                    // We might need to modify it here so that we can.
                    return json.toString();
                };

                final EsBatchIndexer<JsonObject> batchIndexer = new EsBatchIndexer<>(node, func, idExtractor);
                batchIndexer.setIndexName(indexManager.indexName);
                batchIndexer.setTypeName(indexManager.typeName);
                batchIndexer.index(avroStream);
            }

            inputReader.close();
        }

        // First we add all the apps into index.
        if (handleApp) {
            InputStreamReader inputReader = buildStream(rawAppProperties);

            GsonConverter<RawApp> converter = new GsonConverter<RawApp>(RawApp.class);
            LineIterable<RawApp> avroStream = new LineIterable<RawApp>(inputReader, converter);
            final Predicate<RawApp> goodAppFilter = RawToIndexingConverter.getGoodAppTester();
            Iterable<RawApp> filteredStream = new FilteringIterable<RawApp>(avroStream, goodAppFilter);

            LOGGER.info("Creating build job...");
            Function<RawApp, String> idExtractor = app -> {
                return app.android_id;
            };

            final Function<RawApp, String> func = new RawToIndexingConverter(false);

            if (dumpOut) {
                BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/tmpr.txt"));
                for (RawApp app : avroStream) {
                    writer.write(func.apply(app));
                    writer.newLine();
                }
                writer.close();
            } else {
                final EsBatchIndexer<RawApp> batchIndexer = new EsBatchIndexer<>(node, func, idExtractor);
                batchIndexer.setIndexName(indexManager.indexName);
                batchIndexer.setTypeName(indexManager.typeName);
                batchIndexer.indexParallel(batchSize, filteredStream);
            }

            inputReader.close();
        }
        
        if (handleUix){
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

            final EsBatchIndexer<JsonObject> batchIndexer = new EsBatchIndexer<>(node, func, idExtractor);
            batchIndexer.setIndexName(indexManager.indexName);
            batchIndexer.setTypeName(indexManager.typeName);
            batchIndexer.index(uixStream);
            
        }

        LOGGER.info("Indexing complete");
        // Now we need to add the activity crawl for.
        if (!dumpOut) indexManager.close();
    }
}
