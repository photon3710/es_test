package com.jidian.indexing;

import com.jidian.util.BoundedForkJoinPool;
import com.jidian.util.InputStreamBuilder;
import com.jidian.util.ValueAggregator;
import com.jidian.util.iters.ChunkIterable;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Entry point for command-line build tool from Avro file.
 * For now, this is just a template, to make it work, one has to configure the mappers first.
 * <p>
 * We assume that
 */
public class AvroAppIndexingDriver {
    private static final String propertyKey = "defaultProperties";


    public static void main(String[] args) throws IOException {
        Logger LOGGER = LoggerFactory.getLogger(AvroAppIndexingDriver.class);

        Properties property = new Properties();
        FileInputStream in = new FileInputStream(propertyKey);
        property.load(in);
        in.close();

        InputStreamBuilder builder = InputStreamBuilder.getBuilder(property, "");
        InputStream inputStream = builder.buildInputStream();

        DataFileStream<RawApp> avroStream = null;

        try {
            DatumReader<RawApp> datumReader = new SpecificDatumReader<>(RawApp.class);
            avroStream = new DataFileStream<>(inputStream, datumReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Creating build job...");

        IndexManager indexManager = new IndexManager();
        Node node = indexManager.init(property, "");

        final EsBatchIndexer<RawApp> batchIndexer = new EsBatchIndexer<>(node, null, null);
        batchIndexer.setIndexName(indexManager.indexName);
        batchIndexer.setTypeName(indexManager.typeName);

        int parallelism = Runtime.getRuntime().availableProcessors();
        ValueAggregator<Integer> aggregator = ValueAggregator.buildIntegerAggregator();
        BoundedForkJoinPool<Integer> pool = new BoundedForkJoinPool<Integer>(parallelism, aggregator);
        Iterable<List<RawApp>> chunks = new ChunkIterable<RawApp>(parallelism, avroStream);
        for (final List<RawApp> chunk : chunks) {
            Supplier<Integer> command = () -> {
                batchIndexer.index(chunk);
                return new Integer(chunk.size());
            };
            try {
                pool.submitTask(command);
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
        }

        inputStream.close();
        indexManager.close();

        LOGGER.info("Indexing complete");
    }
}