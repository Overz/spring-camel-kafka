package com.github.overz.processors;

import com.github.overz.errors.ApplicationSoapError;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.interceptor.Fault;

@Slf4j
public record SoapInternalServerExceptionProcessor() implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		final var error = exchange.getException(Throwable.class);
		final var fault = new Fault(error);

		if (error instanceof ApplicationSoapError e) {
			fault.setFaultCode(e.getFc());
			fault.setStatusCode(e.getCode().value());
		} else {
			fault.setMessage("Internal Server Error");
		}

//		throw fault;
	}
}
