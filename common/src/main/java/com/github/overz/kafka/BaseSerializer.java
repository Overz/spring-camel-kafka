package com.github.overz.kafka;

import com.github.overz.errors.KafkaSerializationException;
import lombok.Getter;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Getter
public abstract class BaseSerializer<T, B> extends BaseSerdes<B> implements Serializer<T> {
	private Map<String, ?> cofigs;
	private boolean isKey;

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
			if (getOnError().test(e)) {
				throw new KafkaSerializationException("Error serializing data '" + data + "' from topic '" + topic + "'", e);
			}

			return null;
		}
	}

	protected abstract byte[] doSerialize(final T data) throws Exception;
}
