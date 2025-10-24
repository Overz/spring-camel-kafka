package com.github.overz.configs;

import com.github.overz.routes.OrderRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RoutesConfig {

	@Bean
	public OrderRouter orderRouter(
		final ApplicationProperties properties,
		final com.github.overz.services.NotificationService notificationService,
		final com.github.overz.services.MailService mailService
	) {
		return new OrderRouter(
			properties,
			notificationService,
			mailService
		);
	}
}
