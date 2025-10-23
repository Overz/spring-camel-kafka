package com.github.overz.configs;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties implements Serializable {
	private Endpoints endpoints;
	private Topics topics;
	private Files files;
	private Services services;


	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Endpoints implements Serializable {
		private String testService;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Topics implements Serializable {
		private String order;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Files implements Serializable {
		private Resource inputTestFiles;
		private Resource outputTestFiles;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Services implements Serializable {
		private NotificationService notification;


		@Getter
		@Setter
		@Builder
		@AllArgsConstructor
		@NoArgsConstructor
		public static class NotificationService implements Serializable {
			private String baseUrl;
			public String confirmation(final String id) {
				return String.format("%s/confirmation?id=%s", getBaseUrl(), id);
			}
		}
	}
}
