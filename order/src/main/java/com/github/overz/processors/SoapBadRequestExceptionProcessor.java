package com.github.overz.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.interceptor.Fault;

import javax.xml.parsers.DocumentBuilder;

@Slf4j
@RequiredArgsConstructor
public class SoapBadRequestExceptionProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		final var error = exchange.getException(Throwable.class);
		throw new Fault(error, Fault.FAULT_CODE_CLIENT);
	}
}
