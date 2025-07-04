/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.logging;

import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Test;

import org.springframework.boot.logging.DeferredLog.Lines;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link DeferredLog}.
 *
 * @author Phillip Webb
 */
class DeferredLogTests {

	private final DeferredLog deferredLog = new DeferredLog();

	private final Object message = "Message";

	private final Throwable throwable = new IllegalStateException();

	private final Log log = mock(Log.class);

	@Test
	void isTraceEnabled() {
		assertThat(this.deferredLog.isTraceEnabled()).isTrue();
	}

	@Test
	void isDebugEnabled() {
		assertThat(this.deferredLog.isDebugEnabled()).isTrue();
	}

	@Test
	void isInfoEnabled() {
		assertThat(this.deferredLog.isInfoEnabled()).isTrue();
	}

	@Test
	void isWarnEnabled() {
		assertThat(this.deferredLog.isWarnEnabled()).isTrue();
	}

	@Test
	void isErrorEnabled() {
		assertThat(this.deferredLog.isErrorEnabled()).isTrue();
	}

	@Test
	void isFatalEnabled() {
		assertThat(this.deferredLog.isFatalEnabled()).isTrue();
	}

	@Test
	void trace() {
		this.deferredLog.trace(this.message);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().trace(this.message, null);
	}

	@Test
	void traceWithThrowable() {
		this.deferredLog.trace(this.message, this.throwable);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().trace(this.message, this.throwable);
	}

	@Test
	void debug() {
		this.deferredLog.debug(this.message);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().debug(this.message, null);
	}

	@Test
	void debugWithThrowable() {
		this.deferredLog.debug(this.message, this.throwable);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().debug(this.message, this.throwable);
	}

	@Test
	void info() {
		this.deferredLog.info(this.message);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().info(this.message, null);
	}

	@Test
	void infoWithThrowable() {
		this.deferredLog.info(this.message, this.throwable);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().info(this.message, this.throwable);
	}

	@Test
	void warn() {
		this.deferredLog.warn(this.message);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().warn(this.message, null);
	}

	@Test
	void warnWithThrowable() {
		this.deferredLog.warn(this.message, this.throwable);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().warn(this.message, this.throwable);
	}

	@Test
	void error() {
		this.deferredLog.error(this.message);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().error(this.message, null);
	}

	@Test
	void errorWithThrowable() {
		this.deferredLog.error(this.message, this.throwable);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().error(this.message, this.throwable);
	}

	@Test
	void fatal() {
		this.deferredLog.fatal(this.message);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().fatal(this.message, null);
	}

	@Test
	void fatalWithThrowable() {
		this.deferredLog.fatal(this.message, this.throwable);
		this.deferredLog.replayTo(this.log);
		then(this.log).should().fatal(this.message, this.throwable);
	}

	@Test
	void clearsOnReplayTo() {
		this.deferredLog.info("1");
		this.deferredLog.fatal("2");
		Log log2 = mock(Log.class);
		this.deferredLog.replayTo(this.log);
		this.deferredLog.replayTo(log2);
		then(this.log).should().info("1", null);
		then(this.log).should().fatal("2", null);
		then(this.log).shouldHaveNoMoreInteractions();
		then(log2).shouldHaveNoInteractions();
	}

	@Test
	void switchTo() {
		Lines lines = (Lines) ReflectionTestUtils.getField(this.deferredLog, "lines");
		assertThat(lines).isEmpty();
		this.deferredLog.error(this.message, this.throwable);
		assertThat(lines).hasSize(1);
		this.deferredLog.switchTo(this.log);
		assertThat(lines).isEmpty();
		this.deferredLog.info("Message2");
		assertThat(lines).isEmpty();
		then(this.log).should().error(this.message, this.throwable);
		then(this.log).should().info("Message2", null);
	}

}
