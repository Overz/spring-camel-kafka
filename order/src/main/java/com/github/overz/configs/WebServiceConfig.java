package com.github.overz.configs;

import com.github.overz.TestServicePortType;
import com.github.overz.interceptors.SoapRequiredBodyInterceptor;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.cxf.annotations.SchemaValidation.SchemaValidationType;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class WebServiceConfig {
	public static final String SOAP_ENTPOINT_BEAN = "soap-test-endpoint-bean";

	private static final Map<String, SchemaValidationType> validationSchemas = Collections.synchronizedMap(Map.of(
		"processTest", SchemaValidationType.BOTH
	));

	@Bean
	@ConditionalOnMissingBean(ValidatorFactory.class)
	public ValidatorFactory validator() {
		return Validation.buildDefaultValidatorFactory();
	}

	@Bean
	@ConditionalOnMissingBean(SchemaValidationFeature.class)
	public SchemaValidationFeature schemaValidationFeature() {
		return new SchemaValidationFeature(
			operationInfo -> validationSchemas.get(operationInfo.getName().getLocalPart())
		);
	}

	@Bean
	public AbstractPhaseInterceptor<Message> requiredBodyInterceptor() {
		return new SoapRequiredBodyInterceptor();
	}

	@Bean(SOAP_ENTPOINT_BEAN)
	public CxfEndpoint testServiceEndpoint(
		@Value("classpath:wsdl/test.wsdl") Resource resource,
		SchemaValidationFeature schemaValidationFeature,
		@Qualifier("requiredBodyInterceptor") AbstractPhaseInterceptor<Message> requiredBodyInterceptor
	) throws IOException {
		final var endpoint = new CxfEndpoint();

		endpoint.setAddress("/TestService");
		endpoint.setServiceClass(TestServicePortType.class);
		endpoint.setWsdlURL(resource.getURL().toString());
		endpoint.setDataFormat(DataFormat.POJO);
		endpoint.setLoggingFeatureEnabled(true);
		endpoint.setFeatures(new ArrayList<>(List.of(
			schemaValidationFeature
		)));

		endpoint.setInInterceptors(new ArrayList<>(List.of(
			requiredBodyInterceptor
		)));

		return endpoint;
	}
}
