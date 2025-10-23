package com.github.overz.configs;

import com.github.overz.repositories.OrderRepository;
import com.github.overz.routes.OrderRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RoutesConfig {
	@Bean
	public OrderRouter testRouter(
		final ApplicationProperties properties,
		final OrderRepository orderRepository
	) {
		return new OrderRouter(
			properties,
			orderRepository
		);
	}
}
