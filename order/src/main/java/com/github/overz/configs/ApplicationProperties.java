package com.github.overz.configs;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.time.Duration;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties implements Serializable {
	private Services services;
	private Topics topics;
	private Files files;
	private Api api;


	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Services implements Serializable {
		private String orderService;
		private String orderServicePort;
		private String orderClientService;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Topics implements Serializable {
		private String notification;
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
	public static class Api implements Serializable {
		private Duration timeout;
	}
}
