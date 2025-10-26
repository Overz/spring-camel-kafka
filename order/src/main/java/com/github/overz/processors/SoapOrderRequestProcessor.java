package com.github.overz.processors;

import com.github.overz.generated.OrderRequest;
import com.github.overz.utils.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;

@Slf4j
@RequiredArgsConstructor
public class SoapOrderRequestProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		final var data = exchange.getIn().getBody(MessageContentsList.class);
		final var body = (OrderRequest) data.getFirst();
		exchange.getIn().setBody(body.getId());
		exchange.setProperty(Routes.REQUEST_CONTENT_ID, body.getId());
	}
}
