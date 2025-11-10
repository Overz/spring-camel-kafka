package com.github.overz;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

import java.util.Map;

@Slf4j
public class TestRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		log.info("Configuring '{}'", getClass().getName());

		rest()
			.get("/test")
			.routeId("rest-test")
			.produces("application/json")
			.outType(Map.class)
			.description("test endpoint")
			.tag("Test")
			.to("direct:test");


		from("direct:test")
			.routeId("from-test")
			.log("received")
			.setBody(constant(Map.of("ok", true)))
			.end();
	}
}
