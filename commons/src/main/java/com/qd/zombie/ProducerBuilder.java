package com.jidian.util;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringEncoder;

/**
 * This is used to instantiate a Kafka producer so that we can send message to.
 * 
 * ZooKeeper Properties zookeeper.connection.host: ZooKeeper host
 * zookeeper.connection.port: ZooKeeper port
 */
public class ProducerBuilder<K, V> {

	private OptionAccessor accessor;

	public ProducerBuilder(String pfx, Properties prop) {
		accessor = new OptionAccessor(prop, pfx);
	}

	/**
	 * This method simply get the projection of properties, and add some default properties
	 * and create a new producer for logging.
	 * 
	 * @return
	 */
	public Producer<K, V> Build() {
		// configure producer
		Properties properties = accessor.getProjection();
		properties.put("serializer.class", StringEncoder.class.getCanonicalName());
		properties.put("producer.type", "async");
		properties.put("compression.codec", "1");

		return new Producer<K, V>(new ProducerConfig(properties));
	}

}