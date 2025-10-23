package com.github.overz.dtos;

import java.io.Serializable;

public record OrderResponse(
	String result
) implements Serializable {
}
