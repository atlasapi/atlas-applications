package org.atlasapi.application.www;

import org.atlas.application.notification.EmailNotificationSender;
import org.atlasapi.application.auth.LoginController;
import org.atlasapi.application.sources.MongoSourceRequestStore;
import org.atlasapi.application.sources.SourceController;
import org.atlasapi.application.sources.SourceRequestStore;
import org.atlasapi.application.users.v3.UserController;
import org.atlasapi.application.users.v3.UserStore;
import org.atlasapi.application.v3.ApplicationManager;
import org.atlasapi.application.v3.ApplicationStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.auth.RequestScopedAuthenticationProvider;

@Configuration
public class ApplicationWebModule {
	
    @Autowired ApplicationStore appStore;
    @Autowired UserStore userStore;
    @Autowired RequestScopedAuthenticationProvider authProvider;
    @Autowired DatabasedMongo mongo;
    @Autowired EmailNotificationSender emailSender;
    
	@Bean public ApplicationController applicationController() {
        return new ApplicationController(new ApplicationManager(appStore, userStore), authProvider, userStore, sourceRequestStore(), emailSender);
    }

	@Bean public UserController userController() {
	    return new UserController(authProvider, userStore, appStore);
	}
	
	@Bean public SourceController sourceController() {
	    return new SourceController(authProvider, new ApplicationManager(appStore, userStore), userStore, sourceRequestStore(), emailSender);
	}
	
	@Bean public LoginController loginController() {
	    return new LoginController();
	}
	
	@Bean public SourceRequestStore sourceRequestStore() {
	    return new MongoSourceRequestStore(mongo);
	}
}
