package com.github.overz.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.overz.errors.SoapBadRequestException;
import com.github.overz.errors.SoapBadRequestExceptionProcessor;
import com.github.overz.errors.SoapInternalServerException;
import com.github.overz.errors.SoapInternalServerExceptionProcessor;
import com.github.overz.processors.GetSoapPayloadRequestBody;
import com.github.overz.processors.TestResponseProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.xml.parsers.DocumentBuilder;

import static com.github.overz.configs.WebServiceConfig.SOAP_ENTPOINT_BEAN;

@Slf4j
@RequiredArgsConstructor
public class TestRouter extends RouteBuilder {
	private static final String SOAP_TEST_ENTRYPOINT = "cxf:bean:" + SOAP_ENTPOINT_BEAN;
	private final DocumentBuilder documentBuilder;
	private final ObjectMapper objectMapper;

	@Override
	public void configure() throws Exception {
		onException(SoapBadRequestException.class)
			.handled(true)
			.process(new SoapBadRequestExceptionProcessor(documentBuilder))
			.end()
		;

		onException(SoapInternalServerException.class)
			.handled(true)
			.process(new SoapInternalServerExceptionProcessor())
			.end()
		;

		from(SOAP_TEST_ENTRYPOINT)
			.id("test-service")
			.process(new GetSoapPayloadRequestBody())
			.marshal(new JsonDataFormat(JsonLibrary.Jackson))
			.to("kafka:teste")
			.process(new TestResponseProcessor())
			.end()
		;
	}
}
