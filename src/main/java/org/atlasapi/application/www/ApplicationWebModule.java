package org.atlasapi.application.www;

import org.atlasapi.application.persistence.MongoApplicationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
public class ApplicationWebModule {
	
	@Autowired DatabasedMongo mongo;
    
	@Bean public ApplicationController applicationController() {
        return new ApplicationController(applicationStore(), applicationStore());
    }
	
	@Bean public MongoApplicationStore applicationStore(){
		return new MongoApplicationStore(mongo);
	}

}
