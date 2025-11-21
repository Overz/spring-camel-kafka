package com.github.overz.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.dtos.NotificationDto;
import com.github.overz.utils.Beans;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotificationSerializer extends BaseSerializer<NotificationDto, ObjectMapper> {

	@Override
	protected byte[] doSerialize(final NotificationDto data) throws Exception {
		final var mapper = getBean(Beans.OBJECT_MAPPER).orElseGet(ObjectMapper::new);
		return mapper.writeValueAsBytes(data);
	}
}
