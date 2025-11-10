package com.github.overz.routes;

import com.github.overz.configs.ApplicationProperties;
import com.github.overz.dtos.Notification;
import com.github.overz.dtos.OrderRequest;
import com.github.overz.dtos.OrderResponse;
import com.github.overz.errors.SoapBadRequestException;
import com.github.overz.generated.GetOrderRequest;
import com.github.overz.models.Order;
import com.github.overz.models.OrderStatus;
import com.github.overz.processors.*;
import com.github.overz.repositories.OrderRepository;
import com.github.overz.utils.RouteFileDefinition;
import com.github.overz.utils.RouteUtils;
import com.github.overz.utils.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.kafka.KafkaConstants;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

import static com.github.overz.configs.WebServiceConfig.ORDER_CXF_CLIENT;
import static com.github.overz.configs.WebServiceConfig.ORDER_CXF_SERVICE;

@Slf4j
@RequiredArgsConstructor
public class OrderRouter extends RouteBuilder {
	private static final String SOAP_ORDER_ENTRYPOINT = "cxf:bean:" + ORDER_CXF_SERVICE;
	private static final String REST_ORDER_ENTRYPOINT = "direct:rest-order-entrypoint";
	private static final String SOAP_ORDER_CLIENT = "cxf:bean:" + ORDER_CXF_CLIENT;

	private static final String SAVE_ORDER = "direct:save-order";
	private static final String UPDATE_ORDER = "direct:update-order";
	private static final String SEND_TO_KAFKA = "direct:send-do-kafka";
	private static final String SAVE_AND_PUBLISH_AND_CONFIRM = "direct:save-and-publish-and-confirm";
	private static final String CONFIRM_NOTIFICATION_SENT = "direct:confirm-notification-sent";

	private final ApplicationProperties properties;
	private final OrderRepository orderRepository;

	@Override
	public void configure() throws Exception {
		onException(SoapBadRequestException.class)
			.handled(true)
			.process(new SoapBadRequestExceptionProcessor())
			.end()
		;

		onException(Exception.class)
			.handled(true)
			.process(new InternalServerExceptionProcessor())
			.end()
		;

		rest()
			.id(RouteUtils.routeId("rest-order"))
			// @formatter:off
			.post("/v1/order")
				.tag("Order")
				.description("Create an order")
				.consumes("application/json")
				.produces("application/json")
				.type(OrderRequest.class)
				.outType(OrderResponse.class)
				.to(REST_ORDER_ENTRYPOINT)
		// @formatter:on
		;

		from(REST_ORDER_ENTRYPOINT)
			.id(RouteUtils.routeId("redirect-post-order"))
			.log("Order request received, processing...")
			.setProperty(Routes.TYPE, simple("rest"))
			.setProperty(Routes.ORDER_ID, simple("${body.id}"))
			.to(SAVE_AND_PUBLISH_AND_CONFIRM)
			.process(new RestOrderResponseProcessor())
			.end()
		;

		from(SOAP_ORDER_ENTRYPOINT)
			.id(RouteUtils.routeId("order-service"))
			.description("process the request example, save in database '" + Order.TABLE + "' then send to kafka broker's in with topic '" + properties.getTopics().getNotification() + "'")
			.log("Processing request '${id}'")
			.setProperty(Routes.TYPE, simple("soap"))
			.process(new SoapOrderRequestProcessor())
			.to(SAVE_AND_PUBLISH_AND_CONFIRM)
			.log("Creating response body from request id '${id}'")
			.process(new SoapOrderResponseProcessor())
			.log("Response body '${body}' for request id '${id}'")
			.end()
		;

		final var inputFileDefinition = RouteFileDefinition.builder()
			.input(properties.getFiles().getInputTestFiles().getFile().getPath())
			.include(Pattern.compile(".*\\.json$"))
			.noop()
			.idempotent()
			.idempotentKey("${file:name}-${file:modified}")
			.delay(Duration.ofMillis(5L))
			.maxMessagePerPoll(10);

		final var outputFileDefinition = RouteFileDefinition.builder()
			.output(properties.getFiles().getOutputTestFiles().getFile().getPath())
			.fileName("${file:name.noext}_done.${file:ext}")
			.fileExist(RouteFileDefinition.FileExist.OVERRIDE);

		from(inputFileDefinition.build())
			.id(RouteUtils.routeId("test-files"))
			.log("Processing file '${header.CamelFileName}'")
			.setProperty(Routes.TYPE, simple("file"))
			.unmarshal().json()
			// @formatter:off
				.split(body())
				.parallelProcessing()
				.streaming()
				.aggregationStrategy(AggregationStrategies.useOriginal(true))
				.setBody(simple("${body[id]}"))
				.setProperty(Routes.ORDER_ID, simple("${body}"))
				.log("Processing file content '${body}'")
				.to(SAVE_AND_PUBLISH_AND_CONFIRM)
			// @formatter:on
			.end()
			.marshal().json()
			.log("Writing file '${file:name.noext}_done.${file:ext}': '${body}'")
			.to(outputFileDefinition.build())
			.end()
		;

		from(SAVE_AND_PUBLISH_AND_CONFIRM)
			.id(RouteUtils.routeId("save-and-publish"))
			.to(SAVE_ORDER)
			.to(SEND_TO_KAFKA)
			.setProperty(Routes.ORDER_STATUS, constant(OrderStatus.PROCESSING))
			.to(UPDATE_ORDER)
			.to(CONFIRM_NOTIFICATION_SENT)
			.end()
		;

		from(SAVE_ORDER)
			.id(RouteUtils.routeId("save-order"))
			.log("Order '${body}' received, marshalling as json")
			.log("Creating order from request body, request-id: '${id}', body: '${body}'")
			.process(new CreateOrderProcessor())
			.log("Saving order '" + RouteUtils.exP(Routes.ORDER) + "' to database, request-id: '${id}'")
			.bean(orderRepository, "save")
			.setProperty(Routes.ORDER, body())
			.log("Saved order '" + RouteUtils.exP(Routes.ORDER) + "' to database, request-id: '${id}'")
			.end()
		;

		from(UPDATE_ORDER)
			.id(RouteUtils.routeId("update-order"))
			.log("Processing and updating order")
			.log(LoggingLevel.DEBUG, "Change order status ...")
			.process(exchange -> {
				final var status = exchange.getProperty(Routes.ORDER_STATUS, OrderStatus.class);
				final var order = exchange.getProperty(Routes.ORDER, Order.class);
				order.setFlStatus(status);
				exchange.getIn().setBody(order);
			})
			.log("Updating order in database '" + RouteUtils.exP(Routes.ORDER) + "'")
			.bean(orderRepository, "save")
			.setProperty(Routes.ORDER, body())
			.end()
		;

		final var topic = properties.getTopics().getNotification();
		from(SEND_TO_KAFKA)
			.id(RouteUtils.routeId("send-to-kafka"))
			.log("Processing kafka message to send ...")
			.log(LoggingLevel.DEBUG, "Preparing to send message to kafka ...")
			.setBody(exchange -> new Notification(exchange.getProperty(Routes.ORDER_ID, String.class)))
			.removeHeaders("*")
			.setHeader(KafkaConstants.KEY, simple("${id}"))
			.log(LoggingLevel.DEBUG, "Marshaling body '${body}' to json format type ...")
			.marshal().json()
			.log("Sending to kafka, topic: '" + topic + "', key '${id}', value: '${body}'")
			.to(RouteUtils.k(topic))
			.log("Message sent to kafka, topic '" + topic + "', key '${id}', value: '${body}'")
			.end()
		;

		from(CONFIRM_NOTIFICATION_SENT)
			.id(RouteUtils.routeId("confirm-notification-sent"))
			.log("Processing the confirmation notification to send ...")
			.setProperty(Routes.CONFIRMED, simple("false"))
			.removeHeaders("*")
			.loopDoWhile(exchange -> {
				final var confirmed = exchange.getProperty(Routes.CONFIRMED, Boolean.class);
				if (confirmed) {
					return false;
				}

				final var future = Instant.now().plusMillis(properties.getApi().getTimeout().toMillis());
				return !future.isBefore(Instant.now());
			})
			// @formatter:off
				.choice()
					.when(PredicateBuilder.or(
						exchangeProperty(Routes.TYPE).isEqualToIgnoreCase("rest"),
						exchangeProperty(Routes.TYPE).isEqualToIgnoreCase("file")
					))
						.process(exchange -> {
							System.out.println("confirmation rest/file");
						})
						.setHeader(Exchange.HTTP_METHOD, constant("GET"))
						.toD(properties.getApi().getNotification().confirmation("${body.id}"))
//						.validate(exchange -> exchange.getIn().getBody(Notification.class).ok())
						.setProperty(Routes.CONFIRMED, simple("true"))
					.otherwise()
						.process(exchange -> {
							final var order = exchange.getProperty(Routes.ORDER, Order.class);
							final var body = new GetOrderRequest();
							body.setId(order.getData());
							exchange.getIn().setBody(body);
							System.out.println("confirmation soap");
						})
						.setHeader(CxfConstants.OPERATION_NAME, constant("getOrder"))
//						.to(SOAP_ORDER_CLIENT)
//						.validate(exchange -> exchange.getIn().getBody(GetOrderResponse.class).isOk())
						.setProperty(Routes.CONFIRMED, simple("true"))
					.endChoice()
				.delay(3000L)
			// @formatter:on
			.end()
			.log(LoggingLevel.DEBUG, "Notification was sent successfully ...")
		;

		// Asynchronous pipeline that sends to Kafka and retries confirmation for a specific period
//		from(ASYNC_SEND_AND_CONFIRM)
//			.id(Routes.routeId("async-send-and-confirm"))
//			.log("[ASYNC] Starting async send and confirmation for request-id: '${id}'")
//			.process(exchange -> {
//				final long now = System.currentTimeMillis();
//				final long retryUntil = now + 60_000L; // retry for up to 60 seconds
//				exchange.setProperty("confirm.retryUntil", retryUntil);
//				exchange.setProperty("confirm.ok", false);
//			})
//			.loopDoWhile(PredicateBuilder.and(
//				exchange -> exchange.getProperty("confirm.ok", boolean.class),
//				exchange -> exchange.getProperty("confirm.retryUntil", long.class) < System.currentTimeMillis()
//			))
//			// @formatter:off
//			.choice()
//				.when(exchangeProperty(Routes.REQUEST_TYPE).isEqualToIgnoreCase("rest"))
//					.to(WAIT_FOR_CONFIRMATION_REST_ROUTE)
//				.otherwise()
//					.to(WAIT_FOR_CONFIRMATION_SOAP_ROUTE)
//			// @formatter:on
//			.endChoice()
//			.delay(3000) // wait 3 seconds between attempts
//			.end()
//			.log("[ASYNC] Finished confirmation for request-id: '${id}', ok='${exchangeProperty.confirm.ok}'")
//		;
//		final var services = properties.getApi();
//		final var notificationService = services.getNotification();
//		final var confirmNotificationEndpoint = notificationService.confirmation(Routes.exP(Routes.REQUEST_CONTENT_ID));
//		from(WAIT_FOR_CONFIRMATION_REST_ROUTE)
//			.id("wait-for-confirmation-rest")
//			.log("Waiting the confirmation of notification with rest request")
//			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
//			.toD(confirmNotificationEndpoint)
//			.unmarshal().json(Notification.class)
//			.removeHeaders("*")
//			.end()
//		;
//
//		from(WAIT_FOR_CONFIRMATION_SOAP_ROUTE)
//			.id("wait-for-confirmation-soap")
//			.log("Waiting the confirmation of notification with soap request")
//			.process(exchange -> {
//				final var id = exchange.getProperty(Routes.REQUEST_CONTENT_ID, String.class);
//				final var body = new GetOrderRequest();
//				body.setId(id);
//				exchange.getIn().setBody(body);
//			})
//			.setHeader(CxfConstants.OPERATION_NAME, constant("getOrder"))
//			.to(SOAP_ORDER_CLIENT)
//			.process(exchange -> {
//				final var response = exchange.getIn().getBody();
//				if (response instanceof GetOrderResponse r) {
//					exchange.getIn().setBody(new Notification(r.getId(), r.isOk()));
//					exchange.setProperty("confirmation.ok", true);
//				}
//			})
//			.end()
//		;
	}
}
