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

package org.springframework.boot.actuate.endpoint.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.boot.actuate.endpoint.Producible;
import org.springframework.boot.actuate.endpoint.invoke.reflect.OperationMethod;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.Assert;

/**
 * An {@link OperationMethod} discovered by an {@link EndpointDiscoverer}.
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
public class DiscoveredOperationMethod extends OperationMethod {

	private final List<String> producesMediaTypes;

	public DiscoveredOperationMethod(Method method, OperationType operationType,
			AnnotationAttributes annotationAttributes) {
		super(method, operationType, DiscoveredOperationMethod::isOptionalParameter);
		Assert.notNull(annotationAttributes, "'annotationAttributes' must not be null");
		List<String> producesMediaTypes = new ArrayList<>();
		producesMediaTypes.addAll(Arrays.asList(annotationAttributes.getStringArray("produces")));
		producesMediaTypes.addAll(getProducesFromProducible(annotationAttributes));
		this.producesMediaTypes = Collections.unmodifiableList(producesMediaTypes);
	}

	private static boolean isOptionalParameter(Parameter parameter) {
		return MergedAnnotations.from(parameter).isPresent(OptionalParameter.class);
	}

	private <E extends Enum<E> & Producible<E>> List<String> getProducesFromProducible(
			AnnotationAttributes annotationAttributes) {
		Class<?> type = getProducesFrom(annotationAttributes);
		if (type == Producible.class) {
			return Collections.emptyList();
		}
		List<String> produces = new ArrayList<>();
		for (Object value : type.getEnumConstants()) {
			produces.add(((Producible<?>) value).getProducedMimeType().toString());
		}
		return produces;
	}

	private Class<?> getProducesFrom(AnnotationAttributes annotationAttributes) {
		try {
			return annotationAttributes.getClass("producesFrom");
		}
		catch (IllegalArgumentException ex) {
			return Producible.class;
		}
	}

	public List<String> getProducesMediaTypes() {
		return this.producesMediaTypes;
	}

}
