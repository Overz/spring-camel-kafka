package com.github.overz.configs;

import com.github.overz.repositories.NotificationRepository;
import com.github.overz.routes.NotificationRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RoutesConfig {

	@Bean
	public NotificationRouter orderRouter(
		final ApplicationProperties properties,
		final NotificationRepository notificationRepository
	) {
		return new NotificationRouter(
			properties,
			notificationRepository
		);
	}
}
