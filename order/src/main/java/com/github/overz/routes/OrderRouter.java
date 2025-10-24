package com.github.overz.routes;

import com.github.overz.configs.ApplicationProperties;
import com.github.overz.dtos.NotificationResponse;
import com.github.overz.dtos.OrderRequest;
import com.github.overz.dtos.OrderResponse;
import com.github.overz.errors.SoapBadRequestException;
import com.github.overz.errors.StreamingMessageException;
import com.github.overz.generated.GetOrderRequest;
import com.github.overz.generated.GetOrderResponse;
import com.github.overz.models.Order;
import com.github.overz.models.OrderStatus;
import com.github.overz.processors.*;
import com.github.overz.repositories.OrderRepository;
import com.github.overz.utils.RouteFileDefinition;
import com.github.overz.utils.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AggregationStrategies;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.kafka.KafkaConstants;

import java.time.Duration;
import java.util.regex.Pattern;

import static com.github.overz.configs.WebServiceConfig.ORDER_CXF_CLIENT;
import static com.github.overz.configs.WebServiceConfig.ORDER_CXF_SERVICE;

@Slf4j
@RequiredArgsConstructor
public class OrderRouter extends RouteBuilder {
	private static final String SOAP_ORDER_ENTRYPOINT = "cxf:bean:" + ORDER_CXF_SERVICE;
	private static final String REST_ORDER_ENTRYPOINT = "direct:rest-order-endpoint";

	private static final String SEND_TO_KAFKA = "direct:send-do-kafka";
	private static final String SAVE_ORDER = "direct:save-order";
	private static final String SAVE_AND_PUBLISH = "direct:save-and-publish";
	private static final String WAIT_FOR_CONFIRMATION_REST_ROUTE = "direct:wait-for-confirmation-rest";
	private static final String ASYNC_SEND_AND_CONFIRM = "seda:async-send-and-confirm";
	private static final String WAIT_FOR_CONFIRMATION_SOAP_ROUTE = "direct:wait-for-confirmation-soap";
	private static final String SOAP_ORDER_CLIENT = "cxf:bean:" + ORDER_CXF_CLIENT;
	private static final String UPDATE_ORDER = "direct:update-order";

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
			.id("rest-order")
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
			.id("redirect-get-test")
			.setProperty(Routes.REQUEST_TYPE, simple("rest"))
			.setProperty(Routes.REST_REQUEST_BODY, simple("${body}"))
			.setProperty(Routes.REQUEST_CONTENT_ID, simple("${body.id}"))
			.to(SAVE_AND_PUBLISH)
			// Respond immediately without waiting for async confirmation
			.process(exchange -> {
				final var requestId = exchange.getProperty(Routes.REQUEST_CONTENT_ID, String.class);
				exchange.getIn().setBody(new OrderResponse(requestId));
			})
			.end()
		;

		from(SOAP_ORDER_ENTRYPOINT)
			.id("test-service")
			.description("process the request example, save in database '" + Order.TABLE + "' then send to kafka broker's in with topic '" + properties.getTopics().getOrder() + "'")
			.log("Processing request '${id}'")
			.setProperty(Routes.REQUEST_TYPE, simple("soap"))
			.process(new GetSoapPayloadFromRequestBody())
			.to(SAVE_AND_PUBLISH)
			.log("Creating response body from request id '${id}'")
			.process(new TestResponseProcessor())
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
			.id("test-files")
			.log("Processing file '${header.CamelFileName}'")
			.unmarshal().json()
			// @formatter:off
				.split(body())
				.parallelProcessing()
				.streaming()
				.aggregationStrategy(AggregationStrategies.useOriginal())
				.log("Processing content '${body}'")
				.to(SAVE_AND_PUBLISH)
			// @formatter:on
			.end()
			.marshal().json()
			.log("Writing file '${file:name.noext}_done.${file:ext}': '${body}'")
			.to(outputFileDefinition.build())
			.end()
		;

		from(SAVE_AND_PUBLISH)
			.id("save-and-publish")
			.to(SAVE_ORDER)
			// Fire-and-forget: send to Kafka and confirmation asynchronously
			.wireTap(ASYNC_SEND_AND_CONFIRM)
			.end()
		;

		from(SAVE_ORDER)
			.id("save-order")
			.log("Creating order from request body, request-id: '${id}', body: '${body}'")
			.marshal().json()
			.process(new CreateOrderProcessor())
			.log("Saving order '" + Routes.exP(Routes.ORDER) + "' to database, request-id: '${id}'")
			.bean(orderRepository, "save")
			.setProperty(Routes.ORDER, body())
			.log("Saved order '" + Routes.exP(Routes.ORDER) + "' to database, request-id: '${id}'")
			.log("Marshalled order '" + Routes.exP(Routes.ORDER) + "' to json format: '${body}'")
			.setProperty(Routes.KAFKA_VALUE, simple("${body.id}"))
			.setProperty(Routes.KAFKA_KEY, simple("${id}"))
			.end()
		;

		from(UPDATE_ORDER)
			.id("update-order")
			.log("Updating order '" + Routes.exP(Routes.ORDER) + "'")
			.process(exchange -> {
				final var status = exchange.getProperty(Routes.ORDER_STATUS, OrderStatus.class);
				final var order = exchange.getProperty(Routes.ORDER, Order.class);
				order.setFlStatus(status);
				exchange.getIn().setBody(order);
			})
			.bean(orderRepository, "save")
			.setProperty(Routes.ORDER, body())
			.end()
		;

		final var validateKafkaContent = PredicateBuilder.and(
			exchangeProperty(Routes.KAFKA_VALUE).isNull(),
			exchangeProperty(Routes.KAFKA_KEY).isNull()
		);

		final var testTopic = Routes.k(properties.getTopics().getOrder());
		from(SEND_TO_KAFKA)
			.id("send-do-kafka")
			.log("Sending to kafka broker '" + testTopic + "', request-id: '${id}', body: '${body}'")
			// @formatter:off
			.choice()
				.when(validateKafkaContent)
					.throwException(new StreamingMessageException(
						"Property '" + Routes.KAFKA_VALUE + "' or '" + Routes.KAFKA_KEY + "' is null"
					))
				.otherwise()
					.setBody(exchangeProperty(Routes.KAFKA_VALUE))
					.removeHeaders("*")
					.setHeader(KafkaConstants.KEY, exchangeProperty(Routes.KAFKA_KEY))
					.setHeader(KafkaConstants.PARTITION_KEY, constant("0"))
			.endChoice()
			// @formatter:on
			.log("Sending to kafka, topic: '" + testTopic + "', key '" + Routes.exP(Routes.KAFKA_KEY) + "', value: '" + Routes.exP(Routes.KAFKA_VALUE) + "'")
			.to(testTopic)
			.end()
		;

		final var services = properties.getApi();
		final var notificationService = services.getNotification();
		final var confirmNotificationEndpoint = notificationService.confirmation(Routes.exP(Routes.REQUEST_CONTENT_ID));
		from(WAIT_FOR_CONFIRMATION_REST_ROUTE)
			.id("wait-for-confirmation-rest")
			.log("Waiting the confirmation of notification with rest request")
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.toD(confirmNotificationEndpoint)
			.unmarshal().json(NotificationResponse.class)
			.removeHeaders("*")
			.end()
		;

		from(WAIT_FOR_CONFIRMATION_SOAP_ROUTE)
			.id("wait-for-confirmation-soap")
			.log("Waiting the confirmation of notification with soap request")
			.process(exchange -> {
				final var id = exchange.getProperty(Routes.REQUEST_CONTENT_ID, String.class);
				final var body = new GetOrderRequest();
				body.setId(id);
				exchange.getIn().setBody(body);
			})
			.setHeader(CxfConstants.OPERATION_NAME, constant("getOrder"))
			.to(SOAP_ORDER_CLIENT)
			.process(exchange -> {
				final var response = exchange.getIn().getBody();
				if (response instanceof GetOrderResponse r) {
					exchange.getIn().setBody(new NotificationResponse(r.getId(), r.isOk()));
					exchange.setProperty("confirmation.ok", true);
				}
			})
		;

		// Asynchronous pipeline that sends to Kafka and retries confirmation for a specific period
		from(ASYNC_SEND_AND_CONFIRM)
			.id("async-send-and-confirm")
			.log("[ASYNC] Starting async send and confirmation for request-id: '${id}'")
			.to(SEND_TO_KAFKA)
			.setProperty(Routes.ORDER_STATUS, constant(OrderStatus.PROCESSING))
			.to(UPDATE_ORDER)
			.process(exchange -> {
				final long now = System.currentTimeMillis();
				final long retryUntil = now + 60_000L; // retry for up to 60 seconds
				exchange.setProperty("confirm.retryUntil", retryUntil);
				exchange.setProperty("confirm.ok", false);
			})
			.loopDoWhile(PredicateBuilder.and(
				exchange -> exchange.getProperty("confirm.ok", boolean.class),
				exchange -> exchange.getProperty("confirm.retryUntil", long.class) < System.currentTimeMillis()
			))
			// @formatter:off
			.choice()
				.when(exchangeProperty(Routes.REQUEST_TYPE).isEqualToIgnoreCase("rest"))
					.to(WAIT_FOR_CONFIRMATION_REST_ROUTE)
				.otherwise()
					.to(WAIT_FOR_CONFIRMATION_SOAP_ROUTE)
			// @formatter:on
			.endChoice()
			.delay(3000) // wait 3 seconds between attempts
			.end()
			.log("[ASYNC] Finished confirmation for request-id: '${id}', ok='${exchangeProperty.confirm.ok}'")
		;
	}
}
