package com.github.overz.routes;

import com.github.overz.configs.ApplicationProperties;
import com.github.overz.dtos.OrderEvent;
import com.github.overz.generated.GetOrderRequest;
import com.github.overz.generated.GetOrderResponse;
import com.github.overz.services.MailService;
import com.github.overz.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;

import java.time.format.DateTimeFormatter;

import static com.github.overz.configs.WebServiceConfig.ORDER_CXF_SERVICE;

@Slf4j
@RequiredArgsConstructor
public class OrderRouter extends RouteBuilder {
	private static final String SOAP_ORDER_ENTRYPOINT = "cxf:bean:" + ORDER_CXF_SERVICE;

	private final ApplicationProperties properties;
	private final NotificationService notificationService;
	private final MailService mailService;

	@Override
	public void configure() throws Exception {
		// SOAP endpoint: only handle getOrder
		from(SOAP_ORDER_ENTRYPOINT)
			.id("notification-soap-get-order")
			.choice()
				.when(header(CxfConstants.OPERATION_NAME).isEqualTo("getOrder"))
					.process(exchange -> {
						final var req = exchange.getIn().getBody(GetOrderRequest.class);
						final var orderId = req.getOrderId();
						final var opt = notificationService.findByOrderIdCached(orderId);
						final var resp = new GetOrderResponse();
						if (opt.isPresent()) {
							resp.setStatus("FOUND");
							final var createdAt = opt.get().getDtCreatedAt();
							resp.setCreatedAt(createdAt != null ? createdAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null);
						} else {
							resp.setStatus("NOT_FOUND");
						}
						exchange.getMessage().setBody(resp);
					})
				.otherwise()
					.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
					.setBody(simple("Unsupported operation"))
			.end()
		;

		// Kafka consumer: consume order events, save notification, and send email
		final var topic = "kafka:" + properties.getTopics().getOrder();
		from(topic)
			.id("notification-consumer")
			.unmarshal().json(OrderEvent.class)
			.process(exchange -> {
				final var event = exchange.getIn().getBody(OrderEvent.class);
				final var orderId = event.cdOrder();
				log.info("[KAFKA] Received order event for orderId='{}'", orderId);
				notificationService.save(orderId);
				// Send email to a test inbox in MailHog
				mailService.sendOrderNotification("test@example.com", orderId);
			})
			.end();
	}
}
