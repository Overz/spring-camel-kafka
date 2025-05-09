package com.github.overz.processors;

import com.github.overz.models.Order;
import com.github.overz.utils.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

@Slf4j
@RequiredArgsConstructor
public class CreateOrderProcessor implements Processor {
	@Override
	public void process(Exchange exchange) throws Exception {
		final var order = Order.builder().data(exchange.getIn().getBody(String.class)).build();
		exchange.getIn().setBody(order);
		exchange.setProperty(Routes.ORDER, order);
	}
}
