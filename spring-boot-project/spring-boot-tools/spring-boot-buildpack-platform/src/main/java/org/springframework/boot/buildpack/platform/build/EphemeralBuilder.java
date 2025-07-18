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

package org.springframework.boot.buildpack.platform.build;

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.buildpack.platform.docker.type.Image;
import org.springframework.boot.buildpack.platform.docker.type.ImageArchive;
import org.springframework.boot.buildpack.platform.docker.type.ImageArchive.Update;
import org.springframework.boot.buildpack.platform.docker.type.ImageReference;
import org.springframework.boot.buildpack.platform.docker.type.Layer;
import org.springframework.boot.buildpack.platform.io.Content;
import org.springframework.boot.buildpack.platform.io.IOConsumer;
import org.springframework.boot.buildpack.platform.io.Owner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * A short-lived builder that is created for each {@link Lifecycle} run.
 *
 * @author Phillip Webb
 * @author Scott Frederick
 */
class EphemeralBuilder {

	static final String BUILDER_FOR_LABEL_NAME = "org.springframework.boot.builderFor";

	private ImageReference name;

	private final BuildOwner buildOwner;

	private final Creator creator;

	private final BuilderMetadata builderMetadata;

	private final Image builderImage;

	private final IOConsumer<Update> archiveUpdate;

	/**
	 * Create a new {@link EphemeralBuilder} instance.
	 * @param buildOwner the build owner
	 * @param builderImage the base builder image
	 * @param targetImage the image being built
	 * @param builderMetadata the builder metadata
	 * @param creator the builder creator
	 * @param env the builder env
	 * @param buildpacks an optional set of buildpacks to apply
	 */
	EphemeralBuilder(BuildOwner buildOwner, Image builderImage, ImageReference targetImage,
			BuilderMetadata builderMetadata, Creator creator, Map<String, String> env, Buildpacks buildpacks) {
		this.name = ImageReference.random("pack.local/builder/").inTaggedForm();
		this.buildOwner = buildOwner;
		this.creator = creator;
		this.builderMetadata = builderMetadata.copy(this::updateMetadata);
		this.builderImage = builderImage;
		this.archiveUpdate = (update) -> {
			update.withUpdatedConfig(this.builderMetadata::attachTo);
			update.withUpdatedConfig((config) -> config.withLabel(BUILDER_FOR_LABEL_NAME, targetImage.toString()));
			update.withTag(this.name);
			if (!CollectionUtils.isEmpty(env)) {
				update.withNewLayer(getEnvLayer(env));
			}
			if (buildpacks != null) {
				buildpacks.apply(update::withNewLayer);
			}
		};
	}

	private void updateMetadata(BuilderMetadata.Update update) {
		update.withCreatedBy(this.creator.getName(), this.creator.getVersion());
	}

	private Layer getEnvLayer(Map<String, String> env) throws IOException {
		return Layer.of((layout) -> {
			for (Map.Entry<String, String> entry : env.entrySet()) {
				String name = "/platform/env/" + entry.getKey();
				Content content = Content.of((entry.getValue() != null) ? entry.getValue() : "");
				layout.file(name, Owner.ROOT, content);
			}
		});
	}

	/**
	 * Return the name of this archive as tagged in Docker.
	 * @return the ephemeral builder name
	 */
	ImageReference getName() {
		return this.name;
	}

	/**
	 * Return the build owner that should be used for written content.
	 * @return the builder owner
	 */
	Owner getBuildOwner() {
		return this.buildOwner;
	}

	/**
	 * Return the builder meta-data that was used to create this ephemeral builder.
	 * @return the builder meta-data
	 */
	BuilderMetadata getBuilderMetadata() {
		return this.builderMetadata;
	}

	/**
	 * Return the contents of ephemeral builder for passing to Docker.
	 * @param applicationDirectory the application directory
	 * @return the ephemeral builder archive
	 * @throws IOException on IO error
	 */
	ImageArchive getArchive(String applicationDirectory) throws IOException {
		return ImageArchive.from(this.builderImage, (update) -> {
			this.archiveUpdate.accept(update);
			if (StringUtils.hasLength(applicationDirectory)) {
				update.withNewLayer(applicationDirectoryLayer(applicationDirectory));
			}
		});
	}

	private Layer applicationDirectoryLayer(String applicationDirectory) throws IOException {
		return Layer.of((layout) -> layout.directory(applicationDirectory, this.buildOwner));
	}

	@Override
	public String toString() {
		return this.name.toString();
	}

}
