package com.github.overz.errors;

public class KafkaDeserializationException extends ApplicationError {

	public KafkaDeserializationException(String message) {
		this(message, null);
	}

	public KafkaDeserializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
