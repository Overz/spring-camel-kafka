package com.github.overz.processors;

import com.github.overz.generated.OrderResponse;
import com.github.overz.models.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;

@Slf4j
@RequiredArgsConstructor
public class SoapOrderResponseProcessor implements Processor {

	@Override
	public void process(final Exchange exchange) throws Exception {
		final var body = new OrderResponse();
		body.setResult(exchange.getIn().getBody(Order.class).getData());
		exchange.getIn().setBody(new MessageContentsList(body));
	}
}
