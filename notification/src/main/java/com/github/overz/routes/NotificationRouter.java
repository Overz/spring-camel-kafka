package com.github.overz.routes;

import com.github.overz.dtos.Notification;
import com.github.overz.generated.GetOrderRequest;
import com.github.overz.generated.GetOrderResponse;
import com.github.overz.utils.Beans;
import com.github.overz.utils.RouteKafkaDefinition;
import com.github.overz.utils.RouteUtils;
import com.github.overz.utils.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.mail.MailConstants;
import org.apache.camel.model.rest.RestParamType;
import org.apache.cxf.message.MessageContentsList;


@Slf4j
@RequiredArgsConstructor
public class NotificationRouter extends RouteBuilder {
	private static final String SOAP_ORDER_ENTRYPOINT = "cxf:bean:" + Beans.ORDER_CXF_SERVICE;
	private static final String REST_ORDER_ENTRPOINT = "direct:rest-order-entrypoint";
	private static final String SEND_MAIL = "direct:send-mail";
	private static final String SAVE_CONFIRMATION = "direct:save-confirmation";
	private static final String FIND_NOTIFICATION = "direct:find-notification";

	@Override
	public void configure() throws Exception {
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
			.to(REST_ORDER_ENTRPOINT)
		;

		from(REST_ORDER_ENTRPOINT)
			.id(RouteUtils.routeId("redirect-post-order"))
			.log("Order request received, processing...")
			.setBody(header("id"))
			.to(FIND_NOTIFICATION)
			.process(exchange -> {
			})
			.end()
		;

		from(SOAP_ORDER_ENTRYPOINT)
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

		final var notificationKafkaroute = RouteKafkaDefinition.builder()
			.topic("{{app.topics.notification}}")
			.valueSerializerBean(Beans.KAFKA_VALUE_SERIALIZER)
			.valueDeserializerBean(Beans.KAFKA_VALUE_DESERIALIZER)
			.build();

		from(notificationKafkaroute)
			.id(RouteUtils.routeId("notification-consumer"))
			.log("Processing message received: '${body}'")
			.unmarshal().json(Notification.class)
			.setProperty(Routes.NOTIFICATION, body())
			.setProperty(Routes.NOTIFICATION_ID, simple("${body.id}"))
			.to(SEND_MAIL)
			.to(SAVE_CONFIRMATION)
			.end()
		;

		final var notificationIdProperty = RouteUtils.exP(Routes.NOTIFICATION_ID);
		final var mail = RouteUtils.smtp("{{app.mail.smtp.host}}", "{{app.mail.smtp.port}}");
		from(SEND_MAIL)
			.id(RouteUtils.routeId("send-email"))
			.setBody(simple("resource:classpath:templates/email.html"))
			.setHeader(MailConstants.MAIL_FROM, constant("no-reply@example.com"))
			.setHeader(MailConstants.MAIL_TO, simpleF("%s@email.com", notificationIdProperty))
			.setHeader(MailConstants.MAIL_SUBJECT, simpleF("Order - %s", notificationIdProperty))
			.setHeader(MailConstants.MAIL_CONTENT_TYPE, constant("text/html; charset=UTF-8"))
			.log("Sending email message: '${body}")
			.to(mail)
			.end()
		;

		from(SAVE_CONFIRMATION)
			.id(RouteUtils.routeId("save-confirmation"))
			.log("Saving confirmation message: '${body}")
			.setBody(exchangeProperty(Routes.NOTIFICATION))
//			.bean(repo, "save")
			.log("Confirmation message saved: '${body}")
			.end()
		;

		from(FIND_NOTIFICATION)
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
