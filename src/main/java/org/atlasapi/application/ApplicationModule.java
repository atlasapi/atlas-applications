package org.atlasapi.application;

import org.atlasapi.application.persistence.MongoApplicationStore;
import org.atlasapi.application.query.ApiKeyConfigurationFetcher;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.www.ApplicationWebModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
@Import({ApplicationWebModule.class})
public class ApplicationModule {
	
	@Autowired DatabasedMongo mongo;
    
	@Bean ApplicationConfigurationFetcher configFetcher(){
		return new ApiKeyConfigurationFetcher(applicationStore());
	}
	
	@Bean public MongoApplicationStore applicationStore(){
		return new MongoApplicationStore(mongo);
	}
	
}
