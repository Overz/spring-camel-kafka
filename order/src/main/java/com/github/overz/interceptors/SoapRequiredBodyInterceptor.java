package com.github.overz.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.List;

@Slf4j
public class SoapRequiredBodyInterceptor extends AbstractPhaseInterceptor<Message> {

	public SoapRequiredBodyInterceptor() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		final var content = message.getContent(List.class);
		if (content == null || content.isEmpty()) {
			throw new Fault(new IllegalArgumentException("No content"));
		}
	}
}
