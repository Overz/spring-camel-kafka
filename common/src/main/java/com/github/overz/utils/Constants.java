package com.github.overz.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
	public final boolean DEBUG = "true".equalsIgnoreCase(System.getenv("DEBUG"));
}
