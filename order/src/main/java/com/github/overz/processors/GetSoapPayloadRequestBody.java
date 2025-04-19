package com.github.overz.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;

@Slf4j
@RequiredArgsConstructor
public class GetSoapPayloadRequestBody implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		final var data = exchange.getIn().getBody(MessageContentsList.class);
		exchange.getIn().setBody(data.getFirst());
	}
}
