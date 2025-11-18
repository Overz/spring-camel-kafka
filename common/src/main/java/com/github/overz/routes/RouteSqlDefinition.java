package com.github.overz.routes;

import java.util.concurrent.ConcurrentHashMap;

public class RouteSqlDefinition extends BaseRouteDefinition {
	protected RouteSqlDefinition() {
		super(new ConcurrentHashMap<>());
	}

	public static RouteSqlDefinition builder() {
		return new RouteSqlDefinition();
	}

	public RouteSqlDefinition sql(final String v) {
		component("sql:classpath:sql/" + v);
		return this;
	}

	public RouteSqlDefinition datasource(final String bean) {
		append("dataSource", "#" + bean);
		return this;
	}

	public RouteSqlDefinition allowNamedParameters() {
		return allowNamedParameters(true);
	}

	public RouteSqlDefinition allowNamedParameters(final boolean v) {
		append("allowNamedParameters", "" + v);
		return this;
	}

	public RouteSqlDefinition inTransaction() {
		return inTransaction(true);
	}

	public RouteSqlDefinition inTransaction(final boolean v) {
		append("transacted", "" + v);
		return this;
	}

	public RouteSqlDefinition mapper(final String beanName) {
		append("beanRowMapper", "#" + beanName);
		return this;
	}

	public RouteSqlDefinition outClass(final Class<?> cls) {
		append("outputClass", cls.getName());
		return this;
	}

	public RouteSqlDefinition outType(final Enum<?> e) {
		append("outputType", e.name());
		return this;
	}
}
