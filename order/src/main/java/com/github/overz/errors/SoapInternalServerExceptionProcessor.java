package com.github.overz.errors;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.interceptor.Fault;

@Slf4j
public record SoapInternalServerExceptionProcessor() implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		final var error = exchange.getException(SoapInternalServerException.class);
		final var fault = new Fault(error, error.getFc());
		fault.setStatusCode(error.getCode().value());
		throw fault;
	}
}
