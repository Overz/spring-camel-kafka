package com.github.overz.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.dtos.Notification;
import com.github.overz.utils.Beans;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotificationDeserializer extends BaseDeserializer<Notification, ObjectMapper> {

	@Override
	protected Notification doDeserialize(final byte[] data) throws Exception {
		final var mapper = getBean(Beans.OBJECT_MAPPER).orElseGet(ObjectMapper::new);
		return mapper.readValue(data, Notification.class);
	}
}
