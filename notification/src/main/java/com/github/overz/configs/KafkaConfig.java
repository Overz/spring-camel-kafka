package com.github.overz.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.dtos.Notification;
import com.github.overz.serdes.NotificationDeserializer;
import com.github.overz.serdes.NotificationSerializer;
import org.apache.camel.BeanInject;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

@Configuration
public class KafkaConfig {

	@BindToRegistry("valueSerializer")
	public Serializer<Notification> valueSerializer(
		@BeanInject("objectMapper") final ObjectMapper objectMapper
	) {
		return new NotificationSerializer(objectMapper);
	}

	@BindToRegistry("valueDeserializer")
	public Deserializer<Notification> valueDeserializer(
		@BeanInject("objectMapper") final ObjectMapper objectMapper
	) {
		return new NotificationDeserializer(objectMapper);
	}
}
