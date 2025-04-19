package com.github.overz.errors;

import lombok.Getter;
import org.apache.cxf.interceptor.Fault;
import org.springframework.http.HttpStatus;

import javax.xml.namespace.QName;

@Getter
public class SoapBadRequestException extends RuntimeException {
	private final QName fc = Fault.FAULT_CODE_CLIENT;
	private final HttpStatus code = HttpStatus.BAD_REQUEST;

	public SoapBadRequestException(String message) {
		super(message);
	}

	public SoapBadRequestException(String message, Throwable cause) {
		super(message, cause);
	}
}
