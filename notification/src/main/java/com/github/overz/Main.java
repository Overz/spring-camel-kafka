package com.github.overz;

import com.github.overz.kafka.CustomKafkaClientFactory;
import com.github.overz.utils.Beans;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.MainListenerSupport;

import java.util.UUID;

public class Main {
	public static void main(final String[] args) throws Exception {

		final var listener = new MainListenerSupport() {
			@Override
			public void beforeConfigure(final BaseMainSupport main) {
				final var ctx = main.getCamelContext();

				final var pc = ctx.getPropertiesComponent();
				pc.addOverrideProperty("uuid", UUID.randomUUID().toString());

				final var kafka = ctx.getComponent(Beans.KAFKA, KafkaComponent.class);
				kafka.setKafkaClientFactory(new CustomKafkaClientFactory(ctx));
			}
		};

		final var app = new org.apache.camel.main.Main();
		app.addMainListener(listener);
		app.run(args);
	}
}
