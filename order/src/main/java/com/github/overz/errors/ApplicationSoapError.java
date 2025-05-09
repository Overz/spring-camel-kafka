package com.github.overz.errors;

import org.springframework.http.HttpStatus;

import javax.xml.namespace.QName;

public abstract class ApplicationSoapError extends RuntimeException {
	public abstract QName getFc();

	public abstract HttpStatus getCode();

	public ApplicationSoapError(String message) {
		super(message);
	}

	public ApplicationSoapError(String message, Throwable cause) {
		super(message, cause);
	}
}
