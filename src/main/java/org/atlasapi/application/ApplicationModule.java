package org.atlasapi.application;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.query.IpCheckingApiKeyConfigurationFetcher;
import org.atlasapi.application.v3.ApplicationStore;
import org.atlasapi.application.v3.MongoApplicationStore;
import org.atlasapi.persistence.ids.MongoSequentialIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.ViewResolver;

import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.auth.RequestScopedAuthenticationProvider;

@Configuration
@ImportResource("atlas-applications.xml")
public class ApplicationModule {

    @Autowired DatabasedMongo mongo;
    @Autowired ViewResolver viewResolver;
    @Autowired RequestScopedAuthenticationProvider authProvider;
    
	public @Bean ApplicationConfigurationFetcher configFetcher(){
		return new IpCheckingApiKeyConfigurationFetcher(applicationStore());
	}
	
	public @Bean ApplicationStore applicationStore(){
	    IdGenerator idGenerator = new MongoSequentialIdGenerator(mongo, "application");
	    return new MongoApplicationStore(mongo, idGenerator);
	}
}
