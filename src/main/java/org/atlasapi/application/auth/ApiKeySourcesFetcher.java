package org.atlasapi.application.auth;

import javax.servlet.http.HttpServletRequest;
import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationSources;
import org.atlasapi.persistence.application.ApplicationStore;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.social.model.UserRef;

public class ApiKeySourcesFetcher implements ApplicationSourcesFetcher {

    public static final String API_KEY_QUERY_PARAMETER = "apiKey";
    public static final String OAUTH_PROVIDER_QUERY_PARAMETER = "oauth_provider";
    public static final String OAUTH_TOKEN_QUERY_PARAMETER = "oauth_token";
    
    private final ApplicationStore reader;

    public ApiKeySourcesFetcher(ApplicationStore reader) {
        this.reader = reader;
    }
    
    @Override
    public ImmutableSet<String> getParameterNames() {
        return ImmutableSet.of(API_KEY_QUERY_PARAMETER,
                OAUTH_PROVIDER_QUERY_PARAMETER,
                OAUTH_TOKEN_QUERY_PARAMETER);
    }

    @Override
    public Optional<ApplicationSources> sourcesFor(HttpServletRequest request) {
            String apiKey = request.getParameter(API_KEY_QUERY_PARAMETER);
            if (apiKey != null) {
                Optional<Application> app = reader.applicationForKey(apiKey);
                if (app.isPresent()) {
                    return Optional.of(app.get().getSources());
                }
            }
        return Optional.absent();
    }

    @Override
    public Optional<UserRef> userFor(HttpServletRequest request) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
