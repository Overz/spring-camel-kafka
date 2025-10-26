package com.github.overz.dtos;

import java.io.Serializable;

public record Notification(
	String id,
	Boolean ok
) implements Serializable {
}
