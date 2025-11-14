package com.github.overz.kafka;

import com.github.overz.utils.Beans;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.component.kafka.KafkaClientFactory;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"unchecked"})
public class CustomKafkaClientFactory implements KafkaClientFactory {
	private final CamelContext camelContext;

	@Override
	public Producer getProducer(final Properties kafkaProps) {
		final var ks = camelContext.getRegistry().lookupByNameAndType(Beans.KAFKA_KEY_SERIALIZER, Serializer.class);
		final var vs = camelContext.getRegistry().lookupByNameAndType(Beans.KAFKA_VALUE_SERIALIZER, Serializer.class);
		return new KafkaProducer(kafkaProps, ks, vs);
	}

	@Override
	public Consumer getConsumer(final Properties kafkaProps) {
		final var kd = camelContext.getRegistry().lookupByNameAndType(Beans.KAFKA_KEY_DESERIALIZER, Deserializer.class);
		final var vd = camelContext.getRegistry().lookupByNameAndType(Beans.KAFKA_VALUE_DESERIALIZER, Deserializer.class);
		return new KafkaConsumer(kafkaProps, kd, vd);
	}

	@Override
	public String getBrokers(final KafkaConfiguration configuration) {
		return configuration.getBrokers();
	}
}
