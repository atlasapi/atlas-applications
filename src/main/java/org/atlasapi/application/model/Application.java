package org.atlasapi.application.model;

import com.google.common.base.Objects;

public class Application {

	private final String slug;
	private String title;

	private ApplicationConfiguration configuration = new ApplicationConfiguration();

	public Application(String slug) {
		this.slug = slug;
	}

	public String getSlug() {
		return slug;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setConfiguration(ApplicationConfiguration configuration) {
		this.configuration = configuration;
	}

	public ApplicationConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getSlug());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Application) {
			Application otherApp = (Application) other;
			return Objects.equal(getSlug(), otherApp.getSlug());
		}
		return false;
	}

}
