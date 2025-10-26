package com.github.overz.utils;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class Routes {
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

	public String mail(final String host, final String port) {
		return String.format("smtp://%s:%s", host, port);
	}
}
