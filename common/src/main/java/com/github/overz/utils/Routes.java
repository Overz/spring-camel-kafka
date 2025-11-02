package com.github.overz.utils;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class Routes {
	public final String ORDER = "ORDER";
	public final String ORDER_STATUS = "ORDER_STATUS";
	public final String ORDER_ID = "ORDER_ID";
	public final String TYPE = "REQUEST_TYPE";
	public static final String CONFIRMED = "CONFIRMED";

	public String routeId(final String id) {
		return String.format("app-route.%s", id);
	}

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
