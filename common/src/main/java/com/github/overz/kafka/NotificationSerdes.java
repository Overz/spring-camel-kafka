package com.github.overz.kafka;

import com.github.overz.dtos.NotificationDto;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public record NotificationSerdes(
	Serializer<NotificationDto> serializer,
	Deserializer<NotificationDto> deserializer
) implements Serde<NotificationDto> {
}
