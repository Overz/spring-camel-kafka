package com.github.overz.configs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;

@Configuration
public class MapperConfig {

	@BindToRegistry("objectMapper")
	public ObjectMapper objectMapper() {
		return JsonMapper.builder()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.build()
			.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
			;
	}
}
