package org.atlasapi.application.www;

import org.atlasapi.application.Application;

import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class ApplicationModelBuilder implements ModelBuilder<Application>{

	private ApplicationCredentialsModelBuilder credentialsModelBuilder = new ApplicationCredentialsModelBuilder();
	private ApplicationConfigurationModelBuilder configurationModelBuilder = new ApplicationConfigurationModelBuilder();
	
	@Override
	public SimpleModel build(Application application) {
		SimpleModel model = new SimpleModel();
		
		model.put("slug", application.getSlug());
		model.put("title", application.getTitle());
		model.put("description", application.getDescription());
		model.put("created", application.getCreated() != null ? application.getCreated().toString() : null);
		model.put("credentials", credentialsModelBuilder.build(application.getCredentials()));
		model.put("configuration", configurationModelBuilder.build(application.getConfiguration()));
		
		return model;
	}

}
