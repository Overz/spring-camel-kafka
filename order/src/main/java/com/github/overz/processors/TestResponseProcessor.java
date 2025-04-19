package com.github.overz.processors;

import com.github.overz.TestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.cxf.message.MessageContentsList;

@Slf4j
@RequiredArgsConstructor
public class TestResponseProcessor implements Processor {
	@Override
	public void process(Exchange exchange) throws Exception {
		final var body = new TestResponse();
		body.setResult("ok");
		exchange.getMessage().setBody(new MessageContentsList(body));
	}
}
