package com.github.overz.routes;

import com.github.overz.generated.GetOrderRequest;
import com.github.overz.generated.GetOrderResponse;
import com.github.overz.models.Notification;
import com.github.overz.utils.Beans;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.mail.MailConstants;
import org.apache.camel.component.sql.SqlOutputType;
import org.apache.camel.model.rest.RestParamType;
import org.apache.cxf.message.MessageContentsList;

import java.util.ArrayList;


@Slf4j
@NoArgsConstructor
public class NotificationRouter extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		final var soapOrderEntrypoint = "cxf:bean:" + Beans.ORDER_CXF_SERVICE;
		final var restOrderEntrypoint = "direct:rest-order-entrypoint";
		final var sendNotificationEmailRoute = "direct:send-mail";
		final var saveNotificationRoute = "direct:save-confirmation";
		final var findOrderNotificationRoute = "direct:find-order-notification";
		final var notificationKafkaRoute = RouteKafkaDefinition.builder()
			.topic("{{app.topics.notification}}")
			.valueSerializerBean(Beans.KAFKA_VALUE_SERIALIZER)
			.valueDeserializerBean(Beans.KAFKA_VALUE_DESERIALIZER)
			.build();
		final var notificationJdbc = RouteSqlDefinition.builder().datasource(Beans.DATASOURCE);

		rest()
			.get("/v1/notification/{id}")
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
			.produces("application/json")
			.outType(ArrayList.class)
			.to(restOrderEntrypoint)
		;

		from(restOrderEntrypoint)
			.id(RouteUtils.routeId("redirect-post-order"))
			.log("Order request received, processing...")
			.setProperty(Notification.Fields.cdOrder, header("id"))
			.to(findOrderNotificationRoute)
			.removeHeaders("*")
			.convertBodyTo(ArrayList.class)
//			.marshal().json()
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
			.setProperty(Notification.Fields.cdOrder, simple("${body.id}"))
			.to(sendNotificationEmailRoute)
			.to(saveNotificationRoute)
			.end()
		;

		final var eventIdProperty = RouteUtils.exP(Notification.Fields.cdOrder);
		from(sendNotificationEmailRoute)
			.id(RouteUtils.routeId("send-email"))
			.setBody(simple("resource:classpath:templates/email.html", String.class))
			.removeHeaders("*")
			.setHeader(MailConstants.MAIL_FROM, constant("no-reply@example.com"))
			.setHeader(MailConstants.MAIL_TO, simpleF("%s@email.com", eventIdProperty))
			.setHeader(MailConstants.MAIL_SUBJECT, simpleF("Order - %s", eventIdProperty))
			.setHeader(MailConstants.MAIL_CONTENT_TYPE, constant("text/html; charset=UTF-8"))
			.log(LoggingLevel.DEBUG, "Sending email message: '${body}")
			.to(RouteUtils.smtp("{{app.mail.smtp.host}}", "{{app.mail.smtp.port}}"))
			.end()
		;

		final var saveNotificationSql = notificationJdbc.sql("save-notification.sql").build();
		from(saveNotificationRoute)
			.id(RouteUtils.routeId("save-confirmation"))
			.log("Saving confirmation message, sql: '" + saveNotificationSql + "', cdOrder: '" + RouteUtils.exP(Notification.Fields.cdOrder) + "'")
			.removeHeaders("*")
			.to(saveNotificationSql)
			.log("Confirmation notification message saved")
			.end()
		;

		final var findOneNotificationSql = notificationJdbc.sql("find-one-notification.sql")
			.outClass(Notification.class)
			.outType(SqlOutputType.SelectList)
			.build();

		from(findOrderNotificationRoute)
			.id(RouteUtils.routeId("find-order-notification"))
			.removeHeaders("*")
			.log("Finding notification, sql: '" + findOneNotificationSql + "'")
			.to(findOneNotificationSql)
			.log("Notification found: '${body}'")
			.end()
		;
	}
}
