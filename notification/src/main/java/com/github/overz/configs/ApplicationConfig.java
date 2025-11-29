package com.github.overz.configs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.overz.generated.OrderServicePort;
import com.github.overz.kafka.NotificationDeserializer;
import com.github.overz.kafka.NotificationSerializer;
import com.github.overz.utils.Beans;
import com.github.overz.utils.Logs;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.BeanInject;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Configuration;
import org.apache.camel.PropertyInject;
import org.apache.camel.component.cxf.common.DataFormat;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class ApplicationConfig {

	@BindToRegistry(Beans.DATASOURCE)
	public DataSource dataSource(
		@PropertyInject("app.datasource.jdbc-url") final String url,
		@PropertyInject("app.datasource.username") final String username,
		@PropertyInject("app.datasource.password") final String password,
		@PropertyInject("app.datasource.driver") final String driver
	) {
		log.info(Logs.CONFIG_MSG, DataSource.class.getName());
		final var ds = new HikariDataSource();
		ds.setJdbcUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setDriverClassName(driver);
		return ds;
	}

	@BindToRegistry(Beans.OBJECT_MAPPER)
	public ObjectMapper objectMapper() {
		log.info(Logs.CONFIG_MSG, ObjectMapper.class.getName());
		return JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.build()
			.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
			;
	}

	@BindToRegistry(Beans.KAFKA_KEY_SERIALIZER)
	public StringSerializer stringSerializer() {
		return new StringSerializer();
	}

	@BindToRegistry(Beans.KAFKA_KEY_DESERIALIZER)
	public StringDeserializer stringDeserializer() {
		return new StringDeserializer();
	}

	@BindToRegistry(Beans.KAFKA_VALUE_SERIALIZER)
	public NotificationSerializer notificationSerializer() {
		return new NotificationSerializer();
	}

	@BindToRegistry(Beans.KAFKA_VALUE_DESERIALIZER)
	public NotificationDeserializer notificationDeserializer() {
		return new NotificationDeserializer();
	}

	@BindToRegistry(Bus.DEFAULT_BUS_ID)
	public Bus bus() {
		log.info(Logs.CONFIG_MSG, Bus.class.getName());
		final var bus = CXFBusFactory.getDefaultBus(true);
		bus.getInInterceptors().add(new LoggingInInterceptor());
		bus.getOutInterceptors().add(new LoggingOutInterceptor());
		return bus;
	}

	@BindToRegistry(Beans.ORDER_CXF_SERVICE)
	public CxfEndpoint orderCxfService(
		@PropertyInject("app.soap.order.endpoint") final String orderEndpoint,
		@PropertyInject("camel.rest.host") final String host,
		@PropertyInject("app.soap.port") final String port,
		@BeanInject(Bus.DEFAULT_BUS_ID) final Bus bus
	) {
		log.info(Logs.CONFIG_MSG, CxfEndpoint.class.getName());
		final var endpoint = new CxfEndpoint();
		endpoint.setWsdlURL("classpath:wsdl/order.wsdl");
		endpoint.setAddress(String.format("http://%s:%s%s", host, port, orderEndpoint));
		endpoint.setServiceClass(OrderServicePort.class);
		endpoint.setBus(bus);
		endpoint.setDataFormat(DataFormat.POJO);
		endpoint.setLoggingFeatureEnabled(true);
		return endpoint;
	}
}
