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

package org.springframework.boot.actuate.autoconfigure.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional @Conditional} that checks whether logging export is enabled. It
 * matches if the value of the {@code management.logging.export.enabled} property is
 * {@code true} or if it is not configured. If the {@link #value() logging exporter name}
 * is set, the {@code management.<name>.logging.export.enabled} property can be used to
 * control the behavior for the specific logging exporter. In that case, the
 * exporter-specific property takes precedence over the global property.
 *
 * @author Moritz Halbritter
 * @author Dmytro Nosan
 * @since 3.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnEnabledLoggingExportCondition.class)
public @interface ConditionalOnEnabledLoggingExport {

	/**
	 * Name of the logging exporter.
	 * @return the name of the logging exporter
	 */
	String value() default "";

}
