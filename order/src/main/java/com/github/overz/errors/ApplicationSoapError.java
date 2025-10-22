package com.github.overz.errors;

import org.springframework.http.HttpStatus;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Base SOAP error that carries SOAP fault essentials (fault code and HTTP status)
 * and allows subclasses to enrich with specific details.
 */
public abstract class ApplicationSoapError extends ApplicationError {
	private final QName fc;
	private final HttpStatus code;
	private final Map<String, Object> details;

	protected ApplicationSoapError(final String message, final QName faultCode, final HttpStatus httpStatus) {
		this(message, null, faultCode, httpStatus, null);
	}

	protected ApplicationSoapError(final String message, final Throwable cause, final QName faultCode, final HttpStatus httpStatus) {
		this(message, cause, faultCode, httpStatus, null);
	}

	protected ApplicationSoapError(final String message, final Throwable cause, final QName faultCode, final HttpStatus httpStatus, final Map<String, Object> details) {
		super(message, cause);
		this.fc = Objects.requireNonNull(faultCode, "faultCode is required");
		this.code = Objects.requireNonNull(httpStatus, "httpStatus is required");
		this.details = details == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(details));
	}

	public QName getFc() {
		return fc;
	}

	public HttpStatus getCode() {
		return code;
	}

	public Map<String, Object> getDetails() {
		return details;
	}
}
