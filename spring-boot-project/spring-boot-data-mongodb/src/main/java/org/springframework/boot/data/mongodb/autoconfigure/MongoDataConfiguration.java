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

package org.springframework.boot.data.mongodb.autoconfigure;

import java.util.Collections;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoManagedTypes;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * Base configuration class for Spring Data's mongo support.
 *
 * @author Madhura Bhave
 * @author Artsiom Yudovin
 * @author Scott Fredericks
 */
@Configuration(proxyBeanMethods = false)
class MongoDataConfiguration {

	@Bean
	@ConditionalOnMissingBean
	static MongoManagedTypes mongoManagedTypes(ApplicationContext applicationContext) throws ClassNotFoundException {
		return MongoManagedTypes.fromIterable(new EntityScanner(applicationContext).scan(Document.class));
	}

	@Bean
	@ConditionalOnMissingBean
	MongoMappingContext mongoMappingContext(MongoProperties properties, MongoCustomConversions conversions,
			MongoManagedTypes managedTypes) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		MongoMappingContext context = new MongoMappingContext();
		map.from(properties.isAutoIndexCreation()).to(context::setAutoIndexCreation);
		context.setManagedTypes(managedTypes);
		Class<?> strategyClass = properties.getFieldNamingStrategy();
		if (strategyClass != null) {
			context.setFieldNamingStrategy((FieldNamingStrategy) BeanUtils.instantiateClass(strategyClass));
		}
		context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		return context;
	}

	@Bean
	@ConditionalOnMissingBean
	MongoCustomConversions mongoCustomConversions() {
		return new MongoCustomConversions(Collections.emptyList());
	}

	@Bean
	@ConditionalOnMissingBean(MongoConverter.class)
	MappingMongoConverter mappingMongoConverter(ObjectProvider<MongoDatabaseFactory> factory,
			MongoMappingContext context, MongoCustomConversions conversions) {
		MongoDatabaseFactory mongoDatabaseFactory = factory.getIfAvailable();
		DbRefResolver dbRefResolver = (mongoDatabaseFactory != null) ? new DefaultDbRefResolver(mongoDatabaseFactory)
				: NoOpDbRefResolver.INSTANCE;
		MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
		mappingConverter.setCustomConversions(conversions);
		return mappingConverter;
	}

}
