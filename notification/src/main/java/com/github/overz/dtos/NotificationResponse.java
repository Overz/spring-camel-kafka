package com.github.overz.dtos;

import java.io.Serializable;

public record NotificationResponse(
	String id,
	Boolean ok
) implements Serializable {
}
