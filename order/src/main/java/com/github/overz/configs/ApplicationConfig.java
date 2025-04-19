package com.github.overz.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.mappers.JsonMapper;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ApplicationConfig {

	@Bean
	@ConditionalOnMissingBean(ValidatorFactory.class)
	public ValidatorFactory validator() {
		return Validation.buildDefaultValidatorFactory();
	}

	@Bean
	public JsonMapper jsonMapper(
		final ObjectMapper objectMapper
	) {
		return new JsonMapper(objectMapper);
	}
}
