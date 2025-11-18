package com.github.overz.kafka;

import com.github.overz.dtos.NotificationEvent;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public record NotificationSerdes(
	Serializer<NotificationEvent> serializer,
	Deserializer<NotificationEvent> deserializer
) implements Serde<NotificationEvent> {
}
