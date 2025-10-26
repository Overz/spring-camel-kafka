package com.github.overz.dtos;

import java.io.Serializable;

public record NotificationEvent(
	String id,
	Boolean ok
) implements Serializable {
}
