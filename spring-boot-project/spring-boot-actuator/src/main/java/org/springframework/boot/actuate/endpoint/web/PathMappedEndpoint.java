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

package org.springframework.boot.actuate.endpoint.web;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.actuate.endpoint.ExposableEndpoint;

/**
 * Interface that can be implemented by an {@link ExposableEndpoint} that is mapped to a
 * root web path.
 *
 * @author Phillip Webb
 * @since 2.0.0
 * @see PathMapper
 */
@FunctionalInterface
public interface PathMappedEndpoint {

	/**
	 * Return the root path of the endpoint (relative to the context and base path) that
	 * exposes it. For example, a root path of {@code example} would be exposed under the
	 * URL "/{actuator-context}/example".
	 * @return the root path for the endpoint
	 * @see PathMappedEndpoints#getBasePath
	 */
	String getRootPath();

	/**
	 * Return any additional paths (relative to the context) for the given
	 * {@link WebServerNamespace}.
	 * @param webServerNamespace the web server namespace
	 * @return a list of additional paths
	 * @since 3.4.0
	 */
	default List<String> getAdditionalPaths(WebServerNamespace webServerNamespace) {
		return Collections.emptyList();
	}

}
