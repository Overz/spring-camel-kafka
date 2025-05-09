package com.github.overz.configs;

import com.github.overz.repositories.OrderRepository;
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
		final ApplicationProperties properties,
		final DocumentBuilder documentBuilder,
		final OrderRepository orderRepository
	) {
		return new TestRouter(
			properties,
			documentBuilder,
			orderRepository
		);
	}
}
