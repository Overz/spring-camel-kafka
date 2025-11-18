package com.github.overz.routes;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

public class RouteKafkaDefinition extends BaseRouteDefinition {

	private RouteKafkaDefinition() {
		super(new ConcurrentHashMap<>());
	}

	public static RouteKafkaDefinition builder() {
		return new RouteKafkaDefinition();
	}

	@Getter
	@AllArgsConstructor
	public enum OffsetReset {
		EARLIEST("earliest"),
		LATEST("latest"),
		NONE("none");

		private final String value;
	}

	@Getter
	@AllArgsConstructor
	public enum Acks {
		ALL("all"),
		NONE("0"),
		LEADER("1");

		private final String value;
	}

	@Getter
	@AllArgsConstructor
	public enum SecurityProtocol {
		PLAINTEXT("PLAINTEXT"),
		SSL("SSL"),
		SASL_PLAINTEXT("SASL_PLAINTEXT"),
		SASL_SSL("SASL_SSL");

		private final String value;
	}

	@Getter
	@AllArgsConstructor
	public enum CompressionCodec {
		NONE("none"),
		GZIP("gzip"),
		SNAPPY("snappy"),
		LZ4("lz4"),
		ZSTD("zstd");

		private final String value;
	}

	public RouteKafkaDefinition topic(final String topic) {
		component("kafka:" + topic);
		return this;
	}

	public RouteKafkaDefinition brokers(final String brokers) {
		append("brokers", brokers);
		return this;
	}

	public RouteKafkaDefinition clientId(final String clientId) {
		append("clientId", clientId);
		return this;
	}

	public RouteKafkaDefinition groupId(final String groupId) {
		append("groupId", groupId);
		return this;
	}

	public RouteKafkaDefinition autoOffsetReset(final OffsetReset reset) {
		append("autoOffsetReset", reset.getValue());
		return this;
	}

	public RouteKafkaDefinition allowManualCommit(final boolean allow) {
		append("allowManualCommit", Boolean.toString(allow));
		return this;
	}

	public RouteKafkaDefinition autoCommitEnable(final boolean enable) {
		append("autoCommitEnable", Boolean.toString(enable));
		return this;
	}

	public RouteKafkaDefinition autoCommitIntervalMs(final int ms) {
		append("autoCommitIntervalMs", Integer.toString(ms));
		return this;
	}

	public RouteKafkaDefinition keySerializer(final String className) {
		append("keySerializer", className);
		return this;
	}

	public RouteKafkaDefinition valueSerializer(final String className) {
		append("valueSerializer", className);
		return this;
	}

	public RouteKafkaDefinition keySerializerBean(final String beanName) {
		append("keySerializer", "#" + beanName);
		return this;
	}

	public RouteKafkaDefinition valueSerializerBean(final String beanName) {
		append("valueSerializer", "#" + beanName);
		return this;
	}

	public RouteKafkaDefinition keyDeserializer(final String className) {
		append("keyDeserializer", className);
		return this;
	}

	public RouteKafkaDefinition valueDeserializer(final String className) {
		append("valueDeserializer", className);
		return this;
	}

	public RouteKafkaDefinition keyDeserializerBean(final String beanName) {
		append("keyDeserializer", "#" + beanName);
		return this;
	}

	public RouteKafkaDefinition valueDeserializerBean(final String beanName) {
		append("valueDeserializer", "#" + beanName);
		return this;
	}

	public RouteKafkaDefinition enableIdempotence(final boolean flag) {
		append("enableIdempotence", Boolean.toString(flag));
		return this;
	}

	public RouteKafkaDefinition acks(final Acks acks) {
		append("acks", acks.getValue());
		return this;
	}

	public RouteKafkaDefinition compressionCodec(final CompressionCodec codec) {
		append("compressionCodec", codec.getValue());
		return this;
	}

	public RouteKafkaDefinition batchSize(final int batchSize) {
		append("batchSize", Integer.toString(batchSize));
		return this;
	}

	public RouteKafkaDefinition lingerMs(final int ms) {
		append("lingerMs", Integer.toString(ms));
		return this;
	}

	public RouteKafkaDefinition bufferMemory(final long mem) {
		append("bufferMemory", Long.toString(mem));
		return this;
	}

	public RouteKafkaDefinition consumerRequestTimeoutMs(final int ms) {
		append("consumerRequestTimeoutMs", Integer.toString(ms));
		return this;
	}

	public RouteKafkaDefinition maxPollRecords(final int records) {
		append("maxPollRecords", Integer.toString(records));
		return this;
	}

	public RouteKafkaDefinition maxPollIntervalMs(final int ms) {
		append("maxPollIntervalMs", Integer.toString(ms));
		return this;
	}

	public RouteKafkaDefinition fetchMinBytes(final int bytes) {
		append("fetchMinBytes", Integer.toString(bytes));
		return this;
	}

	public RouteKafkaDefinition fetchMaxWaitMs(final int ms) {
		append("fetchMaxWaitMs", Integer.toString(ms));
		return this;
	}

	public RouteKafkaDefinition securityProtocol(final SecurityProtocol protocol) {
		append("securityProtocol", protocol.getValue());
		return this;
	}

	public RouteKafkaDefinition saslMechanism(final String mechanism) {
		append("saslMechanism", mechanism);
		return this;
	}

	public RouteKafkaDefinition saslJaasConfig(final String config) {
		append("saslJaasConfig", config);
		return this;
	}

	public RouteKafkaDefinition sslKeystoreLocation(final String loc) {
		append("sslKeystoreLocation", loc);
		return this;
	}

	public RouteKafkaDefinition sslKeystorePassword(final String pass) {
		append("sslKeystorePassword", pass);
		return this;
	}

	public RouteKafkaDefinition sslTruststoreLocation(final String loc) {
		append("sslTruststoreLocation", loc);
		return this;
	}

	public RouteKafkaDefinition sslTruststorePassword(final String pass) {
		append("sslTruststorePassword", pass);
		return this;
	}

	public RouteKafkaDefinition additionalProperty(final String kafkaKey, final String kafkaValue) {
		append("additionalProperties." + kafkaKey, kafkaValue);
		return this;
	}
}
