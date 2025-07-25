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

package org.springframework.boot.cache.autoconfigure;

import java.util.Properties;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;

/**
 * Callback interface that can be implemented by beans wishing to customize the properties
 * used by the {@link CachingProvider} to create the {@link CacheManager}.
 *
 * @author Stephane Nicoll
 * @since 4.0.0
 * @see CachingProvider#getCacheManager(java.net.URI, ClassLoader, Properties)
 */
public interface JCachePropertiesCustomizer {

	/**
	 * Customize the properties.
	 * @param properties the current properties
	 */
	void customize(Properties properties);

}
