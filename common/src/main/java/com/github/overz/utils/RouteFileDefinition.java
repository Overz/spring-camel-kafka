package com.github.overz.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RouteFileDefinition {
	private final List<String> list = new ArrayList<>();

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
		append("file:" + input);
		return this;
	}

	public RouteFileDefinition output(final String output) {
		append("file:" + output);
		return this;
	}

	public RouteFileDefinition fileName(final String fileName) {
		append("fileName=" + fileName);
		return this;
	}

	public RouteFileDefinition fileExist(final FileExist fileExist) {
		append("fileExist=" + fileExist.getValue());
		return this;
	}

	public RouteFileDefinition include(final Pattern include) {
		append("include=" + include.pattern());
		return this;
	}

	public RouteFileDefinition move(final String move) {
		append("move=" + move);
		return this;
	}

	public RouteFileDefinition moveFailed(final String moveFailed) {
		append("moveFailed=" + moveFailed);
		return this;
	}

	public RouteFileDefinition noop() {
		return noop(true);
	}

	public RouteFileDefinition noop(final Boolean noop) {
		append("noop=" + noop);
		return this;
	}

	public RouteFileDefinition idempotent() {
		return idempotent(true);
	}

	public RouteFileDefinition noIdempotent() {
		return idempotent(false);
	}

	private RouteFileDefinition idempotent(final Boolean idempotent) {
		append("idempotent=" + idempotent);
		return this;
	}

	public RouteFileDefinition idempotentKey(final String idempotentKey) {
		append("idempotentKey=" + idempotentKey);
		return this;
	}

	public RouteFileDefinition delay(final Duration delay) {
		append("delay=" + delay.toMillis());
		return this;
	}

	public RouteFileDefinition maxMessagePerPoll(final Integer maxMessagePerPoll) {
		append("maxMessagesPerPoll=" + maxMessagePerPoll);
		return this;
	}

	public RouteFileDefinition readLock(final ReadLock readLock) {
		append("readLock=" + readLock.getValue());
		return this;
	}

	public RouteFileDefinition readLockCheckInterval(final Integer readLockCheckInterval) {
		append("readLockCheckInterval=" + readLockCheckInterval);
		return this;
	}

	public RouteFileDefinition readLockTimeout(final Duration readLockTimeout) {
		append("readLockTimeout=" + readLockTimeout.toMillis());
		return this;
	}

	private void append(final String... value) {
		if (value == null || value.length == 0) {
			return;
		}

		list.addAll(List.of(value));
	}

	public String build() {
		return build(true);
	}

	public String build(final boolean clean) {
		if (list.isEmpty()) {
			throw new IllegalStateException("No property defined");
		}

		final var first = list.getFirst();
		final var query = list.size() > 1
			? first + "?" + String.join("&", list.subList(1, list.size()))
			: first;

		if (clean) {
			list.clear();
		}

		return query;
	}

}
