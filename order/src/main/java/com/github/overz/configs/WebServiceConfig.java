package com.github.overz.configs;

import com.github.overz.TestServicePortType;
import com.github.overz.interceptors.RequiredBodySoapInterceptor;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.annotations.SchemaValidation.SchemaValidationType;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Configuration
public class WebServiceConfig {
	public static final String SOAP_ENTPOINT_BEAN = "soap-test-endpoint-bean";

	private static final Map<String, SchemaValidationType> validationSchemas = Collections.synchronizedMap(Map.of(
		"processTest", SchemaValidationType.BOTH
	));

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
		return new SchemaValidationFeature(
			operationInfo -> validationSchemas.get(operationInfo.getName().getLocalPart())
		);
	}

	@Bean
	public AbstractSoapInterceptor requiredBodyInterceptor() {
		return new RequiredBodySoapInterceptor();
	}

	@Bean
	public DocumentBuilder documentBuilder() throws ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	@Bean(SOAP_ENTPOINT_BEAN)
	public CxfEndpoint testServiceEndpoint(
		final Bus bus,
		final ApplicationProperties properties,
		@Value("classpath:wsdl/test.wsdl") Resource resource,
		@Qualifier("requiredBodyInterceptor") final AbstractSoapInterceptor requiredBodyInterceptor
	) throws IOException {
		final var endpoint = new CxfEndpoint();

		endpoint.setWsdlURL(resource.getURL().toString());
		endpoint.setAddress(properties.getEndpoints().getTestService());
		endpoint.setServiceClass(TestServicePortType.class);

		endpoint.setInInterceptors(Arrays.asList(
			requiredBodyInterceptor
		));

		endpoint.setBus(bus);
		endpoint.setDataFormat(DataFormat.POJO);
		endpoint.setLoggingFeatureEnabled(true);
		return endpoint;
	}
}
