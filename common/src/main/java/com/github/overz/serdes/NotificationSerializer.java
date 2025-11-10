package com.github.overz.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.dtos.Notification;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationSerializer extends BaseSerializer<Notification> {
	private final ObjectMapper mapper;

	@Override
	protected byte[] doSerialize(final Notification data) throws Exception {
		return this.mapper.writeValueAsBytes(data);
	}
}
