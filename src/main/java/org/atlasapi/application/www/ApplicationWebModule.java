package org.atlasapi.application.www;

import org.atlasapi.application.persistence.ApplicationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationWebModule {
	
	@Autowired ApplicationStore applicationStore;
    
	@Bean public ApplicationController applicationController() {
        return new ApplicationController(applicationStore, applicationStore);
    }

}
