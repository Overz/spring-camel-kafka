package com.github.overz.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.dtos.NotificationDto;
import com.github.overz.utils.Beans;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotificationDeserializer extends BaseDeserializer<NotificationDto, ObjectMapper> {

	@Override
	protected NotificationDto doDeserialize(final byte[] data) throws Exception {
		final var mapper = getBean(Beans.OBJECT_MAPPER).orElseGet(ObjectMapper::new);
		return mapper.readValue(data, NotificationDto.class);
	}
}
