package com.github.overz.dtos;

import java.io.Serializable;

public record NotificationDto(
	String id,
	Boolean ok
) implements Serializable {

	public NotificationDto(String id) {
		this(id, null);
	}
}
