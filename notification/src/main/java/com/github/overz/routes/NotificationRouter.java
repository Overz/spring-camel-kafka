package com.github.overz.routes;

import com.github.overz.dtos.Notification;
import com.github.overz.generated.GetOrderRequest;
import com.github.overz.generated.GetOrderResponse;
import com.github.overz.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.jdbc.JdbcConstants;
import org.apache.camel.component.mail.MailConstants;
import org.apache.camel.model.rest.RestParamType;
import org.apache.cxf.message.MessageContentsList;

import java.util.Map;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
public class NotificationRouter extends RouteBuilder {
	@Override
	public void configure() throws Exception {
		final var soapOrderEntrypoint = "cxf:bean:" + Beans.ORDER_CXF_SERVICE;
		final var restOrderEntrypoint = "direct:rest-order-entrypoint";
		final var sendNotificationEmailRoute = "direct:send-mail";
		final var saveNotificationRoute = "direct:save-confirmation";
		final var findNotificationRoute = "direct:find-notification";
		final var notificationKafkaRoute = RouteKafkaDefinition.builder()
			.topic("{{app.topics.notification}}")
			.valueSerializerBean(Beans.KAFKA_VALUE_SERIALIZER)
			.valueDeserializerBean(Beans.KAFKA_VALUE_DESERIALIZER)
			.build();

		rest()
			.get("/notification")
			.description("Retrieve the notification")
			// @formatter:off
				.param()
					.name("id")
					.type(RestParamType.path)
					.required(true)
					.dataType("string")
					.description("The id sent to notification")
				.endParam()
			// @formatter:on
			.outType(Notification.class)
			.to(restOrderEntrypoint)
		;

		from(restOrderEntrypoint)
			.id(RouteUtils.routeId("redirect-post-order"))
			.log("Order request received, processing...")
			.setBody(header("id"))
			.to(findNotificationRoute)
			.process(exchange -> {
			})
			.end()
		;

		from(soapOrderEntrypoint)
			.id(RouteUtils.routeId("notification-soap-get-order"))
			.choice()
			// @formatter:off
			.when(header(CxfConstants.OPERATION_NAME).isEqualTo("getOrder"))
			.process(exchange -> {
				final var data = exchange.getIn().getBody(MessageContentsList.class);
				final var body = (GetOrderRequest) data.getFirst();

//				final var opt = repo.findByCdOrder(body.getId());
				final var resp = new GetOrderResponse();
				resp.setId(body.getId());
//				resp.setOk(opt.isPresent());
				exchange.getMessage().setBody(resp);
			})
			.otherwise()
			.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
			.setBody(simple("Unsupported operation"))
			// @formatter:on
			.end()
		;

		from(notificationKafkaRoute)
			.id(RouteUtils.routeId("notification-consumer"))
			.log("Processing message received: '${body}'")
			.setProperty(Routes.NOTIFICATION, body())
			.setProperty(Routes.EVENT_ID, simple("${body.id}"))
			.to(sendNotificationEmailRoute)
			.to(saveNotificationRoute)
			.end()
		;

		final var notificationProperty = RouteUtils.exP(Routes.NOTIFICATION);
		final var eventIdProperty = RouteUtils.exP(Routes.EVENT_ID);
		from(sendNotificationEmailRoute)
			.id(RouteUtils.routeId("send-email"))
			.setBody(simple("resource:classpath:templates/email.html"))
			.removeHeaders("*")
			.setHeader(MailConstants.MAIL_FROM, constant("no-reply@example.com"))
			.setHeader(MailConstants.MAIL_TO, simpleF("%s@email.com", eventIdProperty))
			.setHeader(MailConstants.MAIL_SUBJECT, simpleF("Order - %s", eventIdProperty))
			.setHeader(MailConstants.MAIL_CONTENT_TYPE, constant("text/html; charset=UTF-8"))
			.log(LoggingLevel.DEBUG, "Sending email message: '${body}")
			.to(RouteUtils.smtp("{{app.mail.smtp.host}}", "{{app.mail.smtp.port}}"))
			.end()
		;

		final var notificationJdbc = RouteJdbcDefinition.builder()
			.jdbc(Beans.DATASOURCE)
			.useHeadersAsParameters()
			.allowNamedParameters()
			.build();

		from(saveNotificationRoute)
			.id(RouteUtils.routeId("save-confirmation"))
			.log("Saving confirmation message '" + notificationProperty + "'")
			.removeHeaders("*")
			.setBody(simple("resource:classpath:sql/save-notification.sql", String.class))
			// @formatter:off
			.setHeader(JdbcConstants.JDBC_PARAMETERS)
				.exchange(exchange -> Map.of(
					"cdNotification", UUID.randomUUID().toString(),
					"cdOrder", exchange.getProperty(Routes.EVENT_ID)
				))
			// @formatter:on
			.log("Executing SQL '${body}' with the parameters '" + RouteUtils.h(JdbcConstants.JDBC_PARAMETERS) + "'")
			.to(notificationJdbc)
			.log("Confirmation message saved: '${body}")
			.end()
		;

		from(findNotificationRoute)
			.id(RouteUtils.routeId("find-notification"))
			.setBody(exchange -> {
				final var id = exchange.getIn().getBody(String.class);
//				return repo.findByCdOrder(id).orElse(null);
				return null;
			})
			.validate(body().isNotNull())
			.end();
	}
}
