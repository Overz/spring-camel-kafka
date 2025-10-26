package com.github.overz.routes;

import com.github.overz.configs.ApplicationProperties;
import com.github.overz.dtos.NotificationEvent;
import com.github.overz.repositories.NotificationRepository;
import com.github.overz.utils.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mail.MailConstants;

import static com.github.overz.configs.WebServiceConfig.ORDER_CXF_SERVICE;

@Slf4j
@RequiredArgsConstructor
public class NotificationRouter extends RouteBuilder {
	private static final String SOAP_ORDER_ENTRYPOINT = "cxf:bean:" + ORDER_CXF_SERVICE;

	private final ApplicationProperties properties;
	private final NotificationRepository repo;

	private static final String EMAIL = """
		<!DOCTYPE html>
		<html>
		<head>
		    <meta charset="UTF-8">
		    <style>
		        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
		        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
		        .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
		        .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
		        .footer { margin-top: 20px; text-align: center; font-size: 12px; color: #666; }
		        .info { background-color: #e7f3ff; padding: 10px; margin: 10px 0; border-left: 4px solid #2196F3; }
		    </style>
		</head>
		<body>
		    <div class="container">
		        <div class="header">
		            <h2>Notificação do Sistema</h2>
		        </div>
		        <div class="content">
		            <p>Olá,</p>
		            <p>Uma nova mensagem foi processada pelo sistema:</p>
		            <div class="info">
		                <strong>ID da ordem ser serviço:</strong> %s<br>
		            </div>
		            <p>Esta é uma notificação automática. Por favor, não responda este email.</p>
		        </div>
		        <div class="footer">
		            <p>Sistema de Notificações - Apache Camel 4.14</p>
		        </div>
		    </div>
		</body>
		</html>
		""";

	@Override
	public void configure() throws Exception {

		final var mail = Routes.mail(properties.getMail().getSmtpHost(), properties.getMail().getSmtpPort());
		final var topic = properties.getTopics().getNotification();
		from(Routes.k(topic))
			.id(Routes.routeId("notification-consumer"))
			.log("Processing message received: '${body}'")
			.setBody(exchange -> {
				final var notice = exchange.getIn().getBody(NotificationEvent.class);
				return EMAIL.formatted(notice.id());
			})
			.setHeader(MailConstants.MAIL_FROM, constant("no-reply@example.com"))
			.setHeader(MailConstants.MAIL_TO, constant("${body.id}@email.com"))
			.setHeader(MailConstants.MAIL_SUBJECT, constant("Order - ${body.id}"))
			.setHeader(MailConstants.MAIL_CONTENT_TYPE, constant("text/html; charset=UTF-8"))
			.to(mail)
			.end()
		;

//		// SOAP endpoint: only handle getOrder
//		from(SOAP_ORDER_ENTRYPOINT)
//			.id("notification-soap-get-order")
//			.choice()
//				.when(header(CxfConstants.OPERATION_NAME).isEqualTo("getOrder"))
//					.process(exchange -> {
//						final var req = exchange.getIn().getBody(GetOrderRequest.class);
//						final var orderId = req.getOrderId();
//						final var opt = notificationService.findByOrderIdCached(orderId);
//						final var resp = new GetOrderResponse();
//						if (opt.isPresent()) {
//							resp.setStatus("FOUND");
//							final var createdAt = opt.get().getDtCreatedAt();
//							resp.setCreatedAt(createdAt != null ? createdAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
//						} else {
//							resp.setStatus("NOT_FOUND");
//						}
//						exchange.getMessage().setBody(resp);
//					})
//				.otherwise()
//					.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
//					.setBody(simple("Unsupported operation"))
//			.end()
//		;
//
//		// Kafka consumer: consume order events, save notification, and send email
//		final var topic = "kafka:" + properties.getTopics().getOrder();
//		from(topic)
//			.id("notification-consumer")
//			.unmarshal().json(OrderEvent.class)
//			.process(exchange -> {
//				final var event = exchange.getIn().getBody(OrderEvent.class);
//				final var orderId = event.cdOrder();
//				log.info("[KAFKA] Received order event for orderId='{}'", orderId);
//				notificationService.save(orderId);
//				// Send email to a test inbox in MailHog
//				mailService.sendOrderNotification("test@example.com", orderId);
//			})
//			.end();
	}
}
