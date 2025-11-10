package com.github.overz.configs;

import com.github.overz.generated.OrderServicePort;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.PropertyInject;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;

@Configuration
public class WebServiceConfig {
	public static final String ORDER_CXF_SERVICE = "notification-order-cxf-service";

	@BindToRegistry(Bus.DEFAULT_BUS_ID)
	public Bus bus() {
		final var bus = new ExtensionManagerBus();
		bus.getInInterceptors().add(new LoggingInInterceptor());
		bus.getOutInterceptors().add(new LoggingOutInterceptor());
		return bus;
	}

	public CxfEndpoint orderCxfService(
		@PropertyInject("app.soap.order.endpoint") final String orderEndpoint,
		final Bus bus
	) {
		final var endpoint = new CxfEndpoint();
		endpoint.setWsdlURL("classpath:wsdl/order.wsdl");
		endpoint.setAddress(orderEndpoint);
		endpoint.setServiceClass(OrderServicePort.class);
		endpoint.setBus(bus);
		endpoint.setDataFormat(DataFormat.POJO);
		endpoint.setLoggingFeatureEnabled(true);
		return endpoint;
	}
}
