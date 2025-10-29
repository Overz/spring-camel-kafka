package com.github.overz.routes;

import com.github.overz.configs.ApplicationProperties;
import com.github.overz.dtos.NotificationEvent;
import com.github.overz.generated.GetOrderRequest;
import com.github.overz.generated.GetOrderResponse;
import com.github.overz.repositories.NotificationRepository;
import com.github.overz.utils.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.mail.MailConstants;
import org.apache.cxf.message.MessageContentsList;

import static com.github.overz.configs.WebServiceConfig.ORDER_CXF_SERVICE;

@Slf4j
@RequiredArgsConstructor
public class NotificationRouter extends RouteBuilder {
	private static final String SOAP_ORDER_ENTRYPOINT = "cxf:bean:" + ORDER_CXF_SERVICE;
	private static final String SEND_MAIL = "direct:send-mail";
	private static final String SAVE_CONFIRMATION = "direct:save-confirmation";

	private final ApplicationProperties properties;
	private final NotificationRepository repo;

	@Override
	public void configure() throws Exception {
		final var notificationIdProperty = Routes.exP(Routes.NOTIFICATION_ID);
		final var topic = properties.getTopics().getNotification();
		from(Routes.k(topic))
			.id(Routes.routeId("notification-consumer"))
			.log("Processing message received: '${body}'")
			.unmarshal().json(NotificationEvent.class)
			.setProperty(Routes.NOTIFICATION, body())
			.setProperty(Routes.NOTIFICATION_ID, simple("${body.id}"))
			.to(SEND_MAIL)
			.to(SAVE_CONFIRMATION)
			.end()
		;

		final var mail = Routes.mail(properties.getMail().getSmtpHost(), properties.getMail().getSmtpPort());
		from(SEND_MAIL)
			.id(Routes.routeId("send-email"))
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
			.id(Routes.routeId("save-confirmation"))
			.log("Saving confirmation message: '${body}")
			.setBody(exchangeProperty(Routes.NOTIFICATION))
			.bean(repo, "save")
			.log("Confirmation message saved: '${body}")
			.end()
		;

//		// SOAP endpoint: only handle getOrder
		from(SOAP_ORDER_ENTRYPOINT)
			.id(Routes.routeId("notification-soap-get-order"))
			.choice()
			// @formatter:off
				.when(header(CxfConstants.OPERATION_NAME).isEqualTo("getOrder"))
					.process(exchange -> {
						final var data = exchange.getIn().getBody(MessageContentsList.class);
						final var body = (GetOrderRequest) data.getFirst();

						final var opt = repo.findByCdOrder(body.getId());
						final var resp = new GetOrderResponse();
						resp.setId(body.getId());
						resp.setOk(opt.isPresent());
						exchange.getMessage().setBody(resp);
					})
				.otherwise()
					.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
					.setBody(simple("Unsupported operation"))
			// @formatter:on
			.end()
		;
	}
}
