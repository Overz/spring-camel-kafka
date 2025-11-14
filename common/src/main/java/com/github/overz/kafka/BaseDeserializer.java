package com.github.overz.kafka;

import com.github.overz.errors.KafkaDeserializationException;
import lombok.Getter;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Getter
public abstract class BaseDeserializer<T, B> extends BaseSerdes<B> implements Deserializer<T> {
	private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
	private Map<String, ?> configs;
	private boolean isKey;

	@Override
	public void configure(final Map<String, ?> configs, final boolean isKey) {
		this.configs = configs;
		this.isKey = isKey;
	}

	@Override
	public T deserialize(final String s, final byte[] bytes) {
		return this.deserialize(s, null, bytes);
	}

	@Override
	public T deserialize(final String topic, final Headers headers, final byte[] data) {
		return this.deserialize(topic, headers, ByteBuffer.wrap(data));
	}

	@Override
	public T deserialize(final String topic, final Headers headers, final ByteBuffer data) {
		final var buffer = StandardCharsets.UTF_8.decode(data);
		final var decoded = new String(buffer.array());

		try {
			return this.doDeserialize(data.array());
		} catch (Exception e) {
			if (getOnError().test(e)) {
				throw new KafkaDeserializationException("Error deserializing data '" + decoded + "' from topic '" + topic + "'", e);
			}

			return null;
		}
	}

	protected abstract T doDeserialize(final byte[] data) throws Exception;
}
