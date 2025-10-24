package com.github.overz.routes;

import com.github.overz.configs.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
@RequiredArgsConstructor
public class OrderRouter extends RouteBuilder {
	private final ApplicationProperties properties;

	@Override
	public void configure() throws Exception {

	}
}
