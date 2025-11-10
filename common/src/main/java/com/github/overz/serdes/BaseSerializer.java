package com.github.overz.serdes;

import com.github.overz.errors.KafkaSerializationException;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;
import java.util.function.Predicate;

@Getter
public abstract class BaseSerializer<T> implements Serializer<T>, CamelContextAware {
	private Map<String, ?> cofigs;
	private boolean isKey;
	@Setter
	private CamelContext camelContext;

	@Setter
	private Predicate<Exception> onError = e -> false;

	@Override
	public void configure(final Map<String, ?> configs, final boolean isKey) {
		this.cofigs = configs;
		this.isKey = isKey;
	}

	@Override
	public byte[] serialize(final String s, final T t) {
		return this.serialize(s, null, t);
	}

	@Override
	public byte[] serialize(final String topic, final Headers headers, final T data) {
		try {
			return this.doSerialize(data);
		} catch (Exception e) {
			if (this.onError.test(e)) {
				throw new KafkaSerializationException("Error serializing data '" + data + "' from topic '" + topic + "'", e);
			}

			return null;
		}
	}

	protected abstract byte[] doSerialize(final T data) throws Exception;

}
