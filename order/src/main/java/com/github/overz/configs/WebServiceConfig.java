package com.github.overz.configs;

import com.github.overz.TestServicePortType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.cxf.annotations.SchemaValidation.SchemaValidationType;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.feature.validation.SchemaValidationFeature;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
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
	public static final String SOAP_ENDPOINT_BEAN = "testServiceEndpoint";

	private static final Map<String, SchemaValidationType> validationSchemas = Collections.synchronizedMap(Map.of(
		"processTest", SchemaValidationType.BOTH
	));

	@Bean
	public Validator validator() {
		return Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Bean
	@ConditionalOnMissingBean(SchemaValidationFeature.class)
	public SchemaValidationFeature schemaValidationFeature() {
		return new SchemaValidationFeature(
			operationInfo -> validationSchemas.get(operationInfo.getName().getLocalPart())
		);
	}

	@Bean
	public AbstractPhaseInterceptor<SoapMessage> requiredBodyInterceptor() {
		return new AbstractPhaseInterceptor<>(Phase.PRE_INVOKE) {
			@Override
			public void handleMessage(SoapMessage message) throws Fault {
				final var content = message.getContent(List.class);
				if (content == null || content.isEmpty()) {
					throw new Fault(new IllegalArgumentException("No content"));
				}
			}
		};
	}

	@Bean(SOAP_ENDPOINT_BEAN)
	public CxfEndpoint testServiceEndpoint(
		@Value("classpath:wsdl/test.wsdl") Resource resource,
		SchemaValidationFeature schemaValidationFeature,
		@Qualifier("requiredBodyInterceptor") AbstractPhaseInterceptor<SoapMessage> requiredBodyInterceptor
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
