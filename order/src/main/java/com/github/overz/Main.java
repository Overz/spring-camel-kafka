package com.github.overz;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntriesFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class Main {
	public static void main(String[] args) {
		final var env = Dotenv.configure()
			.ignoreIfMissing()
			.systemProperties()
			.load()
			.entries(DotenvEntriesFilter.DECLARED_IN_ENV_FILE)
			.stream()
			.map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
			.toList();

		SpringApplication.run(Main.class, args);

		log.info("Application started with args '{}' and .env '{}'", args, env);
	}
}
