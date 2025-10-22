package com.github.overz.errors;

public class InternalServerException extends ApplicationError {
	public InternalServerException(String message) {
		this(message, null);
	}

	public InternalServerException(Throwable cause) {
		this("Internal Server Error", cause);
	}

	public InternalServerException(String message, Throwable cause) {
		super(message, cause);
	}
}
