package com.github.overz.configs;

import com.github.overz.TestServicePortType;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class WebServiceConfig {
	public static final String SOAP_ENDPOINT_BEAN = "test-service-endpoint";

	@Bean(SOAP_ENDPOINT_BEAN)
	public CxfEndpoint testServiceEndpoint(
		@Value("classpath:wsdl/test.wsdl") Resource resource
	) throws IOException {
		final var endpoint = new CxfEndpoint();
		endpoint.setAddress("/TestService");
		endpoint.setServiceClass(TestServicePortType.class);
		endpoint.setWsdlURL(resource.getURL().toString());
		endpoint.setDataFormat(DataFormat.PAYLOAD);
		endpoint.setLoggingFeatureEnabled(true);
		return endpoint;
	}
}
