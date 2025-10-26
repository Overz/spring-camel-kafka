package com.github.overz.processors;

import com.github.overz.dtos.OrderResponse;
import com.github.overz.utils.Routes;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

@RequiredArgsConstructor
public class RestOrderResponseProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		final var requestId = exchange.getProperty(Routes.REQUEST_CONTENT_ID, String.class);
		exchange.getIn().setBody(new OrderResponse(requestId));
	}
}
