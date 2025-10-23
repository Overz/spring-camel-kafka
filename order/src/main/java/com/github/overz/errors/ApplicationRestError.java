package com.github.overz.errors;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

import java.net.URI;
import java.util.Objects;

/**
 * Base REST error backed by RFC 7807 Problem Details.
 * Subclasses can add specific properties via {@link #withProperty(String, Object)}.
 */
@Getter
public abstract class ApplicationRestError extends ApplicationError implements ErrorResponse {
	private final HttpStatus status;
	private final ProblemDetail body;
	private final HttpHeaders headers = new HttpHeaders();

	protected ApplicationRestError(final HttpStatus status, final String detail) {
		this(status, detail, null);
	}

	protected ApplicationRestError(final HttpStatus status, final String detail, final Throwable cause) {
		super(detail, cause);
		this.status = Objects.requireNonNull(status, "status is required");
		this.body = ProblemDetail.forStatusAndDetail(status, detail);
		this.body.setTitle(getClass().getSimpleName());
		if (cause != null) {
			this.body.setProperty("cause", cause.getClass().getName());
		}
	}

	protected ApplicationRestError(final ProblemDetail body, final HttpStatus status) {
		super(body != null ? body.getDetail() : null);
		this.status = Objects.requireNonNull(status, "status is required");
		this.body = Objects.requireNonNull(body, "body is required");
		if (this.body.getTitle() == null) {
			this.body.setTitle(getClass().getSimpleName());
		}
	}

	// Helpful extension hooks for subclasses
	protected ApplicationRestError withType(URI type) {
		this.body.setType(type);
		return this;
	}

	protected ApplicationRestError withTitle(String title) {
		this.body.setTitle(title);
		return this;
	}

	protected ApplicationRestError withInstance(URI instance) {
		this.body.setInstance(instance);
		return this;
	}

	protected ApplicationRestError withProperty(String name, Object value) {
		this.body.setProperty(name, value);
		return this;
	}
}
