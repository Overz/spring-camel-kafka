package com.github.overz.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.errors.MapperException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JsonMapper {
	private final ObjectMapper mapper;

	public String toJson(Object object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (Exception e) {
			throw new MapperException("Failed to convert object '" + object.getClass().getSimpleName() + "' to json", e);
		}
	}

	public <T> T fromJson(String json, Class<T> clazz) {
		try {
			return mapper.readValue(json, clazz);
		} catch (Exception e) {
			throw new MapperException("Failed to convert json '" + json + "' to object '" + clazz.getSimpleName() + "'", e);
		}
	}
}
