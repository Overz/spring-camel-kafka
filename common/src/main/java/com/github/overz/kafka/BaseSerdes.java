package com.github.overz.kafka;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;

import java.util.Optional;
import java.util.function.Predicate;

@Getter
@Setter
@SuppressWarnings({"unchecked"})
public abstract class BaseSerdes<B> implements CamelContextAware {
	private CamelContext camelContext;
	private Predicate<Exception> onError = e -> false;

	protected Optional<B> getBean(final String name) {
		return getBean(name, null);
	}

	protected Optional<B> getBean(final String name, final Class<B> cls) {
		final var registry = getCamelContext().getRegistry();
		final var bean = cls != null
			? registry.lookupByNameAndType(name, cls)
			: (B) registry.lookupByName(name);

		return Optional.ofNullable(bean);
	}
}
