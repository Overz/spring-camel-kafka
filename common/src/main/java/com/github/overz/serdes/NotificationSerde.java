package com.github.overz.serdes;

import com.github.overz.dtos.Notification;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public record NotificationSerde(
	Serializer<Notification> serializer,
	Deserializer<Notification> deserializer
) implements Serde<Notification> {
}
