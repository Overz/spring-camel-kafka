package com.github.overz.configs;

import com.github.overz.generated.OrderServicePortType;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class WebServiceConfig {
	public static final String ORDER_CXF_SERVICE = "notification-order-cxf-service";

	@Bean(name = Bus.DEFAULT_BUS_ID)
	public Bus bus() {
		return new SpringBus(true);
	}

	@Bean
	public CxfEndpoint baseOrderCxfEndpoint(
		final Bus bus,
		final ApplicationProperties properties,
		@Value("classpath:wsdl/order.wsdl") Resource resource
	) throws IOException {
		final var endpoint = new CxfEndpoint();
		endpoint.setWsdlURL(resource.getURL().toString());
		endpoint.setAddress(properties.getServices().getTestService());
		endpoint.setServiceClass(OrderServicePortType.class);
		endpoint.setBus(bus);
		endpoint.setDataFormat(DataFormat.POJO);
		endpoint.setLoggingFeatureEnabled(true);
		return endpoint;
	}

	@Bean(ORDER_CXF_SERVICE)
	public CxfEndpoint orderCxfService(final CxfEndpoint baseOrderCxfEndpoint) {
		return baseOrderCxfEndpoint.copy();
	}
}
