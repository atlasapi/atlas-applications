package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.persistence.ApplicationReader;

import com.metabroadcast.common.base.Maybe;

public class ApiKeyConfigurationFetcher implements ApplicationConfigurationFetcher {

    public static final String API_KEY_QUERY_PARAMETER = "apiKey";
    private final ApplicationReader reader;
    private final ApplicationConfigurationFetcher delegate;

    public ApiKeyConfigurationFetcher(ApplicationReader reader) {
        this.reader = reader;
        this.delegate  = new IpAddressConfigurationFetcher(reader);
    }

    @Override
    public Maybe<ApplicationConfiguration> configurationFor(HttpServletRequest request) {
        if (request != null) {
            String apiKey = request.getParameter(API_KEY_QUERY_PARAMETER);
            if (apiKey != null) {
                Application app = reader.applicationForKey(apiKey);
                if (app != null) {
                    return Maybe.fromPossibleNullValue(app.getConfiguration());
                }
            } else {
            	return delegate.configurationFor(request);
            }
        }

        return Maybe.nothing();
    }
}
