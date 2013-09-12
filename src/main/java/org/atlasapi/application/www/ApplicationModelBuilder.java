package org.atlasapi.application.www;

import org.atlasapi.application.OldApplication;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.OldApplicationCredentials;

import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class ApplicationModelBuilder implements ModelBuilder<OldApplication>{

	private ModelBuilder<OldApplicationCredentials> credentialsModelBuilder = new ApplicationCredentialsModelBuilder();
	private ModelBuilder<ApplicationConfiguration> configurationModelBuilder = new ApplicationConfigurationModelBuilder();
	
	public ApplicationModelBuilder() {
	    this(new ApplicationConfigurationModelBuilder());
    }
	
	public ApplicationModelBuilder(ModelBuilder<ApplicationConfiguration> configModelBuilder) {
	    this.configurationModelBuilder = configModelBuilder;
	}
	
	@Override
	public SimpleModel build(OldApplication application) {
		SimpleModel model = new SimpleModel();
		
		model.put("slug", application.getSlug());
		model.put("title", application.getTitle());
		model.put("description", application.getDescription());
		model.put("created", application.getCreated() != null ? application.getCreated().toString("yyyy-MM-dd HH:mm:ss") : null);
		model.put("credentials", credentialsModelBuilder.build(application.getCredentials()));
		model.put("configuration", configurationModelBuilder.build(application.getConfiguration()));
		
		return model;
	}

}
