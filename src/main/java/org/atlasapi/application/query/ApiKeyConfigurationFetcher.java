package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.persistence.ApplicationReader;

import com.metabroadcast.common.base.Maybe;

public class ApiKeyConfigurationFetcher implements ApplicationConfigurationFetcher {

    private static final String API_KEY_QUERY_PARAMETER = "apiKey";
    private final ApplicationReader reader;

    public ApiKeyConfigurationFetcher(ApplicationReader reader) {
        this.reader = reader;
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
            }
        }

        return Maybe.nothing();
    }
}
