package org.atlasapi.application.www;

import org.atlasapi.application.Application;

import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class ApplicationModelBuilder implements ModelBuilder<Application>{

	private ApplicationCredentialsModelBuilder credentialsModelBuilder = new ApplicationCredentialsModelBuilder();
	
	@Override
	public SimpleModel build(Application application) {
		SimpleModel model = new SimpleModel();
		
		model.put("slug", application.getSlug());
		model.put("title", application.getTitle());
		model.put("credentials", credentialsModelBuilder.build(application.getCredentials()));
		
		return model;
	}

}
