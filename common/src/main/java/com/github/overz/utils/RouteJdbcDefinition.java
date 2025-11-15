package com.github.overz.utils;

import java.util.concurrent.ConcurrentHashMap;

public class RouteJdbcDefinition extends BaseRouteDefinition {
	protected RouteJdbcDefinition() {
		super(new ConcurrentHashMap<>());
	}

	public static RouteJdbcDefinition builder() {
		return new RouteJdbcDefinition();
	}

	public RouteJdbcDefinition jdbc(final String bean) {
		component("jdbc:" + bean);
		return this;
	}

	public RouteJdbcDefinition useHeadersAsParameters() {
		return useHeadersAsParameters(true);
	}

	public RouteJdbcDefinition useHeadersAsParameters(final boolean v) {
		append("useHeadersAsParameters", "" + v);
		return this;
	}

	public RouteJdbcDefinition allowNamedParameters() {
		return allowNamedParameters(true);
	}

	public RouteJdbcDefinition allowNamedParameters(final boolean v) {
		append("allowNamedParameters", "" + v);
		return this;
	}

	public RouteJdbcDefinition inTransaction() {
		return inTransaction(true);
	}

	public RouteJdbcDefinition inTransaction(final boolean v) {
		append("transacted", "" + v);
		return this;
	}

	public RouteJdbcDefinition mapper(final String beanName) {
		append("beanRowMapper", "#" + beanName);
		return this;
	}
}
