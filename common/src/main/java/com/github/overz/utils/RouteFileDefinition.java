package com.github.overz.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RouteFileDefinition extends BaseRouteDefinition {

	protected RouteFileDefinition() {
		super(new ConcurrentHashMap<>());
	}

	@Getter
	@AllArgsConstructor
	public enum ReadLock {
		NONE("none"),
		MARKER_FILE("markerFile"),
		FILE_LOCK("fileLock"),
		RENAME("rename"),
		CHANGED("changed"),
		IDEMPOTENT("idempotent"),
		IDEMPOTENT_CHANGED("idempotent-changed"),
		IDEMPOTENT_RENAME("idempotent-rename");

		private final String value;
	}

	@Getter
	@AllArgsConstructor
	public enum FileExist {
		OVERRIDE("Override"),
		APPEND("Append"),
		FAIL("Fail"),
		IGNORE("Ignore"),
		MOVE("Move"),
		TRY_RENAME("TryRename"),
		;
		private final String value;
	}

	public static RouteFileDefinition builder() {
		return new RouteFileDefinition();
	}

	public RouteFileDefinition input(final String input) {
		this.component("file:" + input);
		return this;
	}

	public RouteFileDefinition output(final String output) {
		this.component("file:" + output);
		return this;
	}

	public RouteFileDefinition fileName(final String fileName) {
		append("fileName", fileName);
		return this;
	}

	public RouteFileDefinition fileExist(final FileExist fileExist) {
		append("fileExist", fileExist.getValue());
		return this;
	}

	public RouteFileDefinition include(final Pattern include) {
		append("include", include.pattern());
		return this;
	}

	public RouteFileDefinition move(final String move) {
		append("move", move);
		return this;
	}

	public RouteFileDefinition moveFailed(final String moveFailed) {
		append("moveFailed", moveFailed);
		return this;
	}

	public RouteFileDefinition noop() {
		return noop(true);
	}

	public RouteFileDefinition noop(final Boolean noop) {
		append("noop", String.valueOf(noop));
		return this;
	}

	public RouteFileDefinition idempotent() {
		return idempotent(true);
	}

	public RouteFileDefinition noIdempotent() {
		return idempotent(false);
	}

	private RouteFileDefinition idempotent(final Boolean idempotent) {
		append("idempotent", String.valueOf(idempotent));
		return this;
	}

	public RouteFileDefinition idempotentKey(final String idempotentKey) {
		append("idempotentKey", idempotentKey);
		return this;
	}

	public RouteFileDefinition delay(final Duration delay) {
		append("delay", String.valueOf(delay.toMillis()));
		return this;
	}

	public RouteFileDefinition maxMessagePerPoll(final Integer maxMessagePerPoll) {
		append("maxMessagesPerPoll", String.valueOf(maxMessagePerPoll));
		return this;
	}

	public RouteFileDefinition readLock(final ReadLock readLock) {
		append("readLock", readLock.getValue());
		return this;
	}

	public RouteFileDefinition readLockCheckInterval(final Integer readLockCheckInterval) {
		append("readLockCheckInterval", String.valueOf(readLockCheckInterval));
		return this;
	}

	public RouteFileDefinition readLockTimeout(final Duration readLockTimeout) {
		append("readLockTimeout", String.valueOf(readLockTimeout.toMillis()));
		return this;
	}
}
