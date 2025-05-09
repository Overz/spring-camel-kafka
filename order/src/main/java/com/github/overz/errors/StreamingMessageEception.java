package com.github.overz.errors;

import lombok.Getter;
import org.apache.cxf.interceptor.Fault;
import org.springframework.http.HttpStatus;

import javax.xml.namespace.QName;

@Getter
public class StreamingMessageEception extends ApplicationSoapError {
	private final QName fc = Fault.FAULT_CODE_SERVER;
	private final HttpStatus code = HttpStatus.INTERNAL_SERVER_ERROR;

	public StreamingMessageEception(String message) {
		super(message);
	}
}
