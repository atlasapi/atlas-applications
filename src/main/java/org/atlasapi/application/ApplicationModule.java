package org.atlasapi.application;

import static org.atlasapi.application.auth.TwitterAuthController.CALLBACK_URL;
import static org.atlasapi.application.auth.TwitterAuthController.LOGIN_FAILED_URL;
import static org.atlasapi.application.auth.TwitterAuthController.LOGIN_URL;
import static org.atlasapi.application.auth.TwitterAuthController.LOGOUT;

import java.util.List;
import java.util.Map;

import org.atlasapi.application.auth.AdminAuthenticationInterceptor;
import org.atlasapi.application.auth.AuthCallbackHandler;
import org.atlasapi.application.auth.TwitterAuthController;
import org.atlasapi.application.auth.UserAuthCallbackHandler;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.query.IpCheckingApiKeyConfigurationFetcher;
import org.atlasapi.application.users.CacheBackedUserStore;
import org.atlasapi.application.users.MongoUserStore;
import org.atlasapi.application.users.NewUserSupplier;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.application.www.ApplicationWebModule;
import org.atlasapi.persistence.ids.MongoSequentialIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.anonymous.AnonymousUserProvider;
import com.metabroadcast.common.social.anonymous.CookieBasedAnonymousUserProvider;
import com.metabroadcast.common.social.auth.CookieTranslator;
import com.metabroadcast.common.social.auth.DESUserRefKeyEncrypter;
import com.metabroadcast.common.social.auth.RequestScopedAuthenticationProvider;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.auth.credentials.MongoDBCredentialsStore;
import com.metabroadcast.common.social.auth.facebook.AccessTokenChecker;
import com.metabroadcast.common.social.twitter.TwitterApplication;
import com.metabroadcast.common.social.user.AccessTokenProcessor;
import com.metabroadcast.common.social.user.FixedAppIdUserRefBuilder;
import com.metabroadcast.common.social.user.TwitterOAuth1AccessTokenChecker;

@Configuration
@Import({ApplicationWebModule.class})
@ImportResource("atlas-applications.xml")
public class ApplicationModule {
    
	private static final String SALT = "saltthatisofareasonablelength";
    private static final String APP_NAME = "atlas";
    private static final String COOKIE_NAME = "atlastw";

    @Autowired DatabasedMongo mongo;
    @Autowired ViewResolver viewResolver;
    @Autowired RequestScopedAuthenticationProvider authProvider;
	
	@Value("${twitter.auth.consumerKey}") String consumerKey;
	@Value("${twitter.auth.consumerSecret}") String consumerSecret;
	@Value("${local.host.name}") String host;
    
	public @Bean ApplicationConfigurationFetcher configFetcher(){
		return new IpCheckingApiKeyConfigurationFetcher(applicationStore());
	}
	
	public @Bean ApplicationStore applicationStore(){
		return new CacheBackedApplicationStore(new MongoApplicationStore(mongo));
	}
	
	public @Bean UserStore userStore() {
	    return new CacheBackedUserStore(new MongoUserStore(mongo));
	}
	
	public @Bean CredentialsStore credentialsStore() {
	    return new MongoDBCredentialsStore(mongo);
	}

	public @Bean AccessTokenProcessor accessTokenProcessor() {
        AccessTokenChecker accessTokenChecker = new TwitterOAuth1AccessTokenChecker(userRefBuilder() , consumerKey, consumerSecret);
        return new AccessTokenProcessor(accessTokenChecker, credentialsStore());
	}

    public @Bean FixedAppIdUserRefBuilder userRefBuilder() {
        return new FixedAppIdUserRefBuilder(APP_NAME);
    }
    
    public @Bean AnonymousUserProvider anonymousUserProvider(){
        return new CookieBasedAnonymousUserProvider(cookieTranslator(), userRefBuilder());
    }
	
	public @Bean TwitterAuthController authController() {
	    AuthCallbackHandler handler = new UserAuthCallbackHandler(userStore(), new NewUserSupplier(new MongoSequentialIdGenerator(mongo, "users")));
        return new TwitterAuthController(new TwitterApplication(consumerKey, consumerSecret), accessTokenProcessor(), cookieTranslator(),handler, host);
	}

    public @Bean CookieTranslator cookieTranslator() {
        return new CookieTranslator(new DESUserRefKeyEncrypter(SALT), COOKIE_NAME, SALT);
    }
	
    public @Bean DefaultAnnotationHandlerMapping controllerMappings() {
        DefaultAnnotationHandlerMapping controllerClassNameHandlerMapping = new DefaultAnnotationHandlerMapping();
        Object[] interceptors = { getAuthenticationInterceptor() };
        controllerClassNameHandlerMapping.setInterceptors(interceptors);
        return controllerClassNameHandlerMapping;
    }

    public @Bean AdminAuthenticationInterceptor getAuthenticationInterceptor() {
        Map<String, List<String>> methodToPath = Maps.newHashMap();
        
        methodToPath.put("GET", ImmutableList.of("/admin"));
        methodToPath.put("POST", ImmutableList.of("/admin"));
        methodToPath.put("PUT", ImmutableList.of("/admin"));
        methodToPath.put("DELETE", ImmutableList.of("/admin"));
        
        List<String> exceptions = ImmutableList.of(LOGIN_URL, CALLBACK_URL, LOGIN_FAILED_URL, LOGOUT, "/includes/javascript");
        
        AdminAuthenticationInterceptor authenticationInterceptor = new AdminAuthenticationInterceptor();
        authenticationInterceptor.setViewResolver(viewResolver);
        authenticationInterceptor.setLoginView("redirect:/admin/login");
        authenticationInterceptor.setAuthService(authProvider);
        authenticationInterceptor.setAuthenticationRequiredByMethod(methodToPath);
        authenticationInterceptor.setExceptions(exceptions);
        authenticationInterceptor.setUserStore(userStore());
        return authenticationInterceptor;
    }
}
