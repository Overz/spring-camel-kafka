package com.github.overz.dtos;

import com.github.overz.utils.Routes;
import org.apache.camel.Exchange;

import java.io.Serializable;

public record Notification(
	String id,
	Boolean ok
) implements Serializable {

	public Notification(String id) {
		this(id, null);
	}

	public Notification(Exchange exchange) {
		this(exchange.getProperty(Routes.ORDER_ID, String.class));
	}
}
