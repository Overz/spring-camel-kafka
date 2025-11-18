package com.github.overz.routes;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseRouteDefinition {
	private String componentUri;
	private final Map<String, String> map;

	protected void component(final String componentUri) {
		if (StringUtils.isEmpty(componentUri)) {
			throw new IllegalArgumentException("Component URI is empty/null");
		}

		this.componentUri = componentUri;
	}

	protected void append(final String key, final String value) {
		if (StringUtils.isAllEmpty(key, value)) {
			return;
		}

		if (key.contains("=") || value.contains("=")) {
			throw new IllegalArgumentException("Key and Value cannot contain the special character '='");
		}

		this.map.put(key, value);
	}

	public String build() {
		if (componentUri == null) {
			throw new IllegalStateException("No component defined");
		}

		if (map.isEmpty()) {
			return componentUri;
		}

		final String query = map.entrySet()
			.stream()
			.map(e -> e.getKey() + "=" + e.getValue())
			.collect(Collectors.joining("&"));

		return componentUri + "?" + query;
	}
}
