package com.github.overz.errors;

public class KafkaSerializationException extends ApplicationError {
	public KafkaSerializationException(String message) {
		this(message, null);
	}

	public KafkaSerializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
