package com.github.overz.dtos;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRestResponse implements Serializable {
	private String result;
}
