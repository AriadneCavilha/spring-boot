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

package org.springframework.boot.mongodb.autoconfigure;

import java.util.List;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.internal.MongoClientImpl;

/**
 * Tests for {@link MongoClientFactory}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author Mark Paluch
 * @author Scott Frederick
 */
class MongoClientFactoryTests extends MongoClientFactorySupportTests<MongoClient> {

	@Override
	protected MongoClient createMongoClient(List<MongoClientSettingsBuilderCustomizer> customizers,
			MongoClientSettings settings) {
		return new MongoClientFactory(customizers).createMongoClient(settings);
	}

	@Override
	protected MongoClientSettings getClientSettings(MongoClient client) {
		return ((MongoClientImpl) client).getSettings();
	}

}
