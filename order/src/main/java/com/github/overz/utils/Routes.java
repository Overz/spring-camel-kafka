package com.github.overz.utils;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class Routes {
	public final String SOAP_REQUEST_BODY = "SOAP_REQUEST_BODY";
	public final String REST_REQUEST_BODY = "REST_REQUEST_BODY";
	public final String ORDER = "ORDER";
	public final String REQUEST_CONTENT_ID = "REQUEST_CONTENT_ID";
	public final String KAFKA_VALUE = "TO_KAFKA_VALUE";
	public final String KAFKA_KEY = "TO_KAFKA_KEY";
	public final String REQUEST_TYPE = "REQUEST_TYPE";

	public String exP(final String v) {
		return String.format("${exchangeProperty.%s}", v);
	}

	public String multiExP(final String... v) {
		return String.join(",", Arrays.stream(v).map(Routes::exP).toList());
	}

	public String k(final String v) {
		return String.format("kafka:%s", v);
	}
}
