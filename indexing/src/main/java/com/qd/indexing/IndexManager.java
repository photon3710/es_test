package com.qd.indexing;

import com.qd.util.OptionAccessor;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.node.NodeBuilder.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This is used to create the index on elasticsearch.
 * There are three requirements for json object that goes into build.
 * 1. retrieval
 * 2. rescore
 * 3. display
 *
 * For retrieval purpose, we need the following sections
 * a. keywords (appname, search words from log, maybe developer name as well).
 * b. description for app, what is new, release notes.
 * c. filtered reviews.
 *
 * For rescoring purpose, we need the following sections, mainly for quality signals.
 * a. average reviews
 * b. average review count.
 * c. average downloads
 * d. spam scoring
 *
 * For display purpose, just description for now for text.
 * a. icon_url
 * b. app_url
 *
 * Created by xiaoyun on 4/18/14.
 */

public class IndexManager {

    private static final String indexStr = "index";
    private static final String typeStr = "type";
    private static final String numOfShardsStr = "number_of_shards";
    private static final String numOfReplicasStr = "number_of_replicas";

    private static final String defaultIndex = "testqd";
    private static final String defaultType = "testuix";

    private final int offset_gap = 32;
    private final static String analyzerName = "standard";

    public String indexName;
    public String typeName;

    // We will need this to be alive during the build.
    Node node;


    /**
     * Here we initialize the EsBatchIndexer, where we set up global index option
     * such as index/type, we also, figure
     * @param prop
     * @param prefix
     *
     */
    public Node init(Properties prop, String prefix) {
        OptionAccessor accessor = new OptionAccessor(prop, prefix);
        indexName = accessor.getValue(indexStr, defaultIndex);
        typeName = accessor.getValue(typeStr, defaultType);

        node = nodeBuilder().clusterName("qd").client(true).node();
        Client client = node.client();

        try {
            createIndex(accessor, client);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return node;
    }

    public XContentBuilder getMapping() throws IOException {

        // Build the mappings source
        XContentBuilder mappingSource = XContentFactory.jsonBuilder().prettyPrint()
                .startObject()
                .startObject("properties")
                .startObject(UixField.INTENT.getKeyStr())
                .field("type", "string")
                .field("index", "no")
                .endObject()
                .startObject(UixField.UIXFILENAME.getKeyStr())
                .field("type", "string")
                .field("index", "no")
                .endObject()
                .startObject(UixField.PATH.getKeyStr())
                .field("type", "string")
                .field("index", "analyzed")
                .field("include_in_all", true)
                .field("analyzer", analyzerName)
                .field("index_options", "positions")
                .startObject("norms")
                .field("enabled", true)
                .endObject()
                .endObject()
                .startObject(UixField.CONTENT.getKeyStr())
                .field("type", "string")
                .field("index", "analyzed")
                .field("include_in_all", true)
                .field("analyzer", analyzerName)
                .field("index_options", "positions")
                .startObject("norms")
                .field("enabled", true)
                .endObject()
                .endObject()

                .endObject()   // end of properties
                .startObject("_all")
                .field("index", "analyzed")
                .field("index_options", "freqs")
                .startObject("norms")
                .field("enabled", false)
                .endObject()
                .field("position_offset_gap", offset_gap)
                .endObject()
                .endObject();

        return mappingSource;
    }


    /**
     * This method create index and setup the mapping for build.
     *
     * @param accessor
     * @param client
     * @throws IOException
     */
    
    private void createIndex(OptionAccessor accessor, Client client) throws IOException {
        int numOfShards = Integer.parseInt(accessor.getValue(numOfShardsStr, "1"));
        int numOfReplicas = Integer.parseInt(accessor.getValue(numOfReplicasStr, "0"));

        // Build the settings
        XContentBuilder settings = XContentFactory.jsonBuilder()
                .startObject()
                    .field("number_of_shards", numOfShards)
                    .field("number_of_replicas", numOfReplicas)
                    .field("analysis.analyzer.default.type", analyzerName) 
                .endObject();

        client.admin().indices().prepareCreate(indexName).setSettings(settings).execute().actionGet();

        XContentBuilder mapping = getMapping();

        client.admin().indices()
                .preparePutMapping(indexName).setType(typeName)
                .setSource(mapping).setIndices(indexName)
                .execute().actionGet();
    }


    public void close() throws IOException {
        Client client = node.client();
        client.admin().indices().prepareFlush(indexName).execute().actionGet();
        client.admin().indices().prepareRefresh(indexName).setForce(true).execute().actionGet();
        node.close();
    }
}
