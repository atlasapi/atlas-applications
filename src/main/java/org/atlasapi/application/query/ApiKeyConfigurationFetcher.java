package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;
import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationSources;
import org.atlasapi.persistence.application.ApplicationStore;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class ApiKeyConfigurationFetcher implements ApplicationSourcesFetcher {

    public static final String API_KEY_QUERY_PARAMETER = "apiKey";
    
    private final ApplicationStore reader;

    public ApiKeyConfigurationFetcher(ApplicationStore reader) {
        this.reader = reader;
    }
    
    @Override
    public ImmutableSet<String> getParameterNames() {
        return ImmutableSet.of(API_KEY_QUERY_PARAMETER);
    }

    @Override
    public Optional<ApplicationSources> sourcesFor(HttpServletRequest request) {
        if (request != null) {
            String apiKey = request.getParameter(API_KEY_QUERY_PARAMETER);
            if (apiKey != null) {
                Optional<Application> app = reader.applicationForKey(apiKey);
                if (app.isPresent()) {
                    return Optional.of(app.get().getSources());
                }
            }
        }
        return Optional.absent();
    }   
}
