package com.github.overz.interceptors;

import com.github.overz.errors.SoapBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import java.util.List;

@Slf4j
public class RequiredBodySoapInterceptor extends AbstractSoapInterceptor {
	public RequiredBodySoapInterceptor() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		final var content = message.getContent(List.class);
		if (content == null || content.isEmpty()) {
			throw new SoapBadRequestException("Required body is empty");
		}
	}
}
