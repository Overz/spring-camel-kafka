package com.github.overz.errors;

import org.apache.cxf.interceptor.Fault;
import org.springframework.http.HttpStatus;

public class SoapBadRequestException extends ApplicationSoapError {
	public SoapBadRequestException(String message) {
		super(message, Fault.FAULT_CODE_CLIENT, HttpStatus.BAD_REQUEST);
	}

	public SoapBadRequestException(String message, Throwable cause) {
		super(message, cause, Fault.FAULT_CODE_CLIENT, HttpStatus.BAD_REQUEST);
	}
}
