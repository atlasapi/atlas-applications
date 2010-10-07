package org.atlasapi.application;

public class Application {

	private final String slug;
	private String title;
	
	private ApplicationConfiguration configuration; 

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

}
