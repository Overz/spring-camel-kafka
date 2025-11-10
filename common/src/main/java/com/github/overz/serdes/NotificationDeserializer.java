package com.github.overz.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.dtos.Notification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationDeserializer extends BaseDeserializer<Notification> {
	private final ObjectMapper mapper;

	@Override
	protected Notification doDeserialize(final byte[] data) throws Exception {
		return mapper.readValue(data, Notification.class);
	}
}
