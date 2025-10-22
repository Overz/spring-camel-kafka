package com.github.overz.errors;

import org.apache.cxf.interceptor.Fault;
import org.springframework.http.HttpStatus;

public class SoapInternalServerException extends ApplicationSoapError {
	public SoapInternalServerException() {
		this("Internal Server Error", null);
	}

	public SoapInternalServerException(Throwable cause) {
		this("Internal Server Error", cause);
	}

	private SoapInternalServerException(String message, Throwable cause) {
		super(message, cause, Fault.FAULT_CODE_SERVER, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
