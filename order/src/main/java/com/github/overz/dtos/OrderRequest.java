package com.github.overz.dtos;

import java.io.Serializable;

public record OrderRequest(
	String id
) implements Serializable {
}
