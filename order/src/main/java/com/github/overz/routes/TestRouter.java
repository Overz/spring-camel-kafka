package com.github.overz.routes;

import com.github.overz.TestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.stereotype.Component;

import static com.github.overz.configs.WebServiceConfig.SOAP_ENDPOINT_BEAN;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestRouter extends RouteBuilder {
	private static final String SOAP_TEST_ENTRYPOINT = "cxf:bean:" + SOAP_ENDPOINT_BEAN;

	@Override
	public void configure() throws Exception {
		from(SOAP_TEST_ENTRYPOINT)
			.id("test-service")
			.log("RECEIVED!")
			.process(exchange -> {
				System.out.println("ok");

				var body = new TestResponse();
				body.setResult("ok");

				exchange.getMessage().setBody(new MessageContentsList(body));
			})
			.end()
		;
	}
}
