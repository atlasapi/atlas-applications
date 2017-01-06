package org.atlasapi.application.query;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.MoreObjects;
import com.metabroadcast.applications.client.ApplicationsClient;
import com.metabroadcast.applications.client.model.internal.Application;
import com.metabroadcast.applications.client.model.internal.Environment;
import com.metabroadcast.applications.client.query.Query;
import com.metabroadcast.applications.client.query.Result;
import com.metabroadcast.common.properties.Configurer;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApiKeyApplicationFetcher implements ApplicationFetcher {

    public static final String API_KEY_QUERY_PARAMETER = "apiKey";
    private final Environment environment;
    
    private final ApplicationsClient applicationsClient;

    public ApiKeyApplicationFetcher(ApplicationsClient applicationsClient) {
        this.applicationsClient = applicationsClient;
        this.environment = checkNotNull(Environment.parse(Configurer.getPlatform()));
    }

    @Override
    public Optional<Application> applicationFor(HttpServletRequest request)
            throws InvalidApiKeyException {
        String apiKey = MoreObjects.firstNonNull(
                request.getParameter(API_KEY_QUERY_PARAMETER),
                request.getHeader(API_KEY_QUERY_PARAMETER)
        );
        if (apiKey != null) {
            Result result = applicationsClient.resolve(Query.create(apiKey, environment));
            if (result.getErrorCode().isPresent()) {
                throw new InvalidApiKeyException(apiKey, result.getErrorCode().get());
            }

            return result.getSingleResult();
        }
        return Optional.empty();
    }
}
