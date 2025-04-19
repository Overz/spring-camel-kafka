package com.github.overz.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.routes.TestRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.parsers.DocumentBuilder;

@Slf4j
@Configuration
public class RoutesConfig {
	@Bean
	public TestRouter testRouter(
		final DocumentBuilder documentBuilder,
		final ObjectMapper objectMapper
	) {
		return new TestRouter(
			documentBuilder,
			objectMapper
		);
	}
}
