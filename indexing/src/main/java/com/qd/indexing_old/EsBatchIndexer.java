package com.jidian.indexing;

import com.google.gson.JsonObject;
import com.jidian.util.BoundedForkJoinPool;
import com.jidian.util.OptionAccessor;
import com.jidian.util.ValueAggregator;
import com.jidian.util.iters.ChunkIterable;
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
 *
 * Created by xiaoyun on 4/18/14.
 */
public class EsBatchIndexer<T> {
    private static Logger LOGGER = LoggerFactory.getLogger(EsBatchIndexer.class);

    String indexName;
    String typeName;

    // We will need this to be alive during the build.
    Node node;

    final Function<T, String> mapper;
    final Function<T, String> idExtractor;

    EsBatchIndexer(Node node, final Function<T, String> m, final Function<T, String> f) {
        mapper = m;
        this.node = node;
        idExtractor = f;
    }

    public void setIndexName(String index) {
        indexName = index;
    }

    public void setTypeName(String type) {
        typeName = type;
    }


    /**
     * This method does indexing in parallel, with specified batch size. The parallelism is determined
     * by number of cpu core for now.
     * @param batchSize
     * @param avroStream
     */
    public void indexParallel(int batchSize, Iterable<T> avroStream) {
        int parallelism = Runtime.getRuntime().availableProcessors();
        ValueAggregator<Integer> aggregator = ValueAggregator.buildIntegerAggregator();
        BoundedForkJoinPool<Integer> pool = new BoundedForkJoinPool<Integer>(parallelism, aggregator);
        Iterable<List<T>> chunks = new ChunkIterable<T>(batchSize, avroStream);
        for (final List<T> chunk : chunks) {
            try {
                pool.submitTask(() -> {
                    index(chunk);
                    return new Integer(chunk.size());
                });
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }
    }


    public void index(Iterable<T> apps) {
        Client client = node.client();
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        int count = 0;
        for (T app : apps) {
            count += 1;
            IndexRequest indexRequest = new IndexRequest(indexName, typeName, idExtractor.apply(app));
            // Build the document source and save it in request.
            indexRequest.source(mapper.apply(app));
            bulkRequest.add(indexRequest);
        }

        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if (bulkResponse.hasFailures()) {
            // process failures by iterating through each bulk response item
            BulkItemResponse[] items = bulkResponse.getItems();
            for (BulkItemResponse item : items) {
                if (item.isFailed()) {
                    LOGGER.info(item.getFailureMessage());
                }
            }
            throw new RuntimeException("Found " + items.length + " BulkIndexErrors");
        }
        LOGGER.info("Finished " + count + " items...");
    }
}
