package com.github.overz.processors;

import com.github.overz.errors.ApplicationError;
import com.github.overz.errors.ApplicationSoapError;
import com.github.overz.errors.InternalServerException;
import com.github.overz.utils.Routes;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.interceptor.Fault;


@Slf4j
public class InternalServerExceptionProcessor implements Processor {

	@Override
	public void process(final Exchange exchange) throws Exception {
		final var error = exchange.getException(Throwable.class);
		final var type = exchange.getProperty(Routes.TYPE, String.class);

		if (error instanceof ApplicationError ex) {
			if ("rest".equalsIgnoreCase(type)) {
				throw new InternalServerException(error);
			} else if ("soap".equalsIgnoreCase(type)) {
				final var e = (ApplicationSoapError) ex;
				final var fault = new Fault(e);
				fault.setFaultCode(e.getFc());
				fault.setStatusCode(e.getCode().value());
				fault.setMessage(e.getMessage());
				throw fault;
			} else {
				throw new InternalServerException(error);
			}
		}
	}
}
