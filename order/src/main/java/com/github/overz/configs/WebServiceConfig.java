package com.github.overz.configs;

import com.github.overz.generated.OrderServicePort;
import com.github.overz.interceptors.RequiredBodySoapInterceptor;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.annotations.SchemaValidation.SchemaValidationType;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;

@Configuration
public class WebServiceConfig {
	public static final String ORDER_CXF_SERVICE = "order-cxf-service";
	public static final String ORDER_CXF_CLIENT = "order-cxf-client";

	@Bean(name = Bus.DEFAULT_BUS_ID)
	public Bus bus(
		final SchemaValidationFeature schemaValidationFeature
	) {
		final var bus = new SpringBus(true);
		bus.getFeatures().add(schemaValidationFeature);
		return bus;
	}

	@Bean
	@ConditionalOnMissingBean(SchemaValidationFeature.class)
	public SchemaValidationFeature schemaValidationFeature() {
		return new SchemaValidationFeature(operationInfo -> SchemaValidationType.BOTH);
	}

	@Bean
	public AbstractSoapInterceptor requiredBodyInterceptor() {
		return new RequiredBodySoapInterceptor();
	}

	@Bean
	public DocumentBuilder documentBuilder() throws ParserConfigurationException {
		return DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
	}

	@Bean
	public CxfEndpoint baseOrderCxfEndpoint(
		final Bus bus,
		final ApplicationProperties properties
	) throws IOException {
		final var endpoint = new CxfEndpoint();
		endpoint.setWsdlURL("classpath:wsdl/order.wsdl");
		endpoint.setAddress(properties.getServices().getOrderService());
		endpoint.setServiceClass(OrderServicePort.class);
		endpoint.setBus(bus);
		endpoint.setDataFormat(DataFormat.POJO);
		endpoint.setLoggingFeatureEnabled(true);
		return endpoint;
	}

	@Bean(ORDER_CXF_SERVICE)
	public CxfEndpoint orderCxfService(
		@Qualifier("baseOrderCxfEndpoint") final CxfEndpoint base,
		@Qualifier("requiredBodyInterceptor") final AbstractSoapInterceptor requiredBodyInterceptor,
		final ApplicationProperties properties
	) {
		final var endpoint = base.copy();
		endpoint.setInInterceptors(Arrays.asList(
			requiredBodyInterceptor
		));
//		endpoint.setPublishedEndpointUrl(properties.getServices().getOrderService());
		return endpoint;
	}

	@Bean(ORDER_CXF_CLIENT)
	public CxfEndpoint orderCxfClient(
		@Qualifier("baseOrderCxfEndpoint") final CxfEndpoint base,
		final ApplicationProperties properties
	) {
		final var endpoint = base.copy();
		endpoint.setAddress(properties.getServices().getOrderClientService());
		return endpoint;
	}
}
