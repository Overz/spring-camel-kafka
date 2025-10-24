package com.github.overz.errors;

public abstract class ApplicationError extends RuntimeException {
	public ApplicationError(String message) {
		this(message, null);
	}

	public ApplicationError(Throwable cause) {
		this(null, cause);
	}

	public ApplicationError(String message, Throwable cause) {
		super(message, cause);
	}
}
