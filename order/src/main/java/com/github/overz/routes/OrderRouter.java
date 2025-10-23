package com.github.overz.routes;

import com.github.overz.configs.ApplicationProperties;
import com.github.overz.dtos.NotificationResponse;
import com.github.overz.dtos.OrderRequest;
import com.github.overz.dtos.OrderResponse;
import com.github.overz.errors.SoapBadRequestException;
import com.github.overz.errors.StreamingMessageException;
import com.github.overz.models.Order;
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
import org.apache.camel.component.kafka.KafkaConstants;

import java.time.Duration;
import java.util.regex.Pattern;

import static com.github.overz.configs.WebServiceConfig.SOAP_ENTPOINT_BEAN;

@Slf4j
@RequiredArgsConstructor
public class OrderRouter extends RouteBuilder {
	private static final String SOAP_TEST_ENTRYPOINT = "cxf:bean:" + SOAP_ENTPOINT_BEAN;
	private static final String REST_TEST_ENDPOINT = "direct:rest-test-endpoint";

	private static final String SEND_TO_KAFKA = "direct:send-do-kafka";
	private static final String SAVE_ORDER = "direct:save-order";
	private static final String SAVE_AND_PUBLISH = "direct:save-and-publish";
	private static final String WAIT_FOR_CONFIRMATION = "direct:wait-for-confirmation";

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
			.id("rest-test")
			// @formatter:off
			.post("/v1/test")
				.tag("Testing")
				.description("Test the endpoint")
				.consumes("application/json")
				.produces("application/json")
				.type(OrderRequest.class)
				.outType(OrderResponse.class)
				.to(REST_TEST_ENDPOINT)
		// @formatter:on
		;

		from(REST_TEST_ENDPOINT)
			.id("redirect-get-test")
			.setProperty(Routes.REQUEST_TYPE, simple("rest"))
			.setProperty(Routes.REST_REQUEST_BODY, simple("${body}"))
			.setProperty(Routes.REQUEST_CONTENT_ID, simple("${body.id}"))
			.to(SAVE_AND_PUBLISH)
			.process(exchange -> {
				final var notice = exchange.getIn().getBody(NotificationResponse.class);
				final var body = new OrderResponse(notice.id());
				exchange.getIn().setBody(body);
			})
			.end()
		;

		from(SOAP_TEST_ENTRYPOINT)
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
			.threads(10)
			.to(SEND_TO_KAFKA)
			.to(WAIT_FOR_CONFIRMATION)
			.end()
		;

		from(SAVE_ORDER)
			.id("save-order")
			.log("Creating order from request body, request-id: '${id}', body: '${body}'")
			.marshal().json()
			.process(new CreateOrderProcessor())
			.log("Saving order '" + Routes.exP(Routes.ORDER) + "' to database, request-id: '${id}'")
			.bean(orderRepository, "save")
			.log("Saved order '" + Routes.exP(Routes.ORDER) + "' to database, request-id: '${id}'")
			.marshal().json()
			.log("Marshalled order '" + Routes.exP(Routes.ORDER) + "' to json format: '${body}'")
			.setProperty(Routes.KAFKA_VALUE, simple("${body}"))
			.setProperty(Routes.KAFKA_KEY, simple("${id}"))
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

		final var services = properties.getServices();
		final var notificationService = services.getNotification();
		final var confirmNotificationEndpoint = notificationService.confirmation(Routes.exP(Routes.REQUEST_CONTENT_ID));
		from(WAIT_FOR_CONFIRMATION)
			.id("wait-for-confirmation")
			.log("Waiting the confirmation of notification")
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.toD(confirmNotificationEndpoint)
			.unmarshal().json(NotificationResponse.class)
			.removeHeaders("*")
		;
	}
}
