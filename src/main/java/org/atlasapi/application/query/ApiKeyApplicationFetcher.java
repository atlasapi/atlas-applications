package org.atlasapi.application.query;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.metabroadcast.applications.client.ApplicationsClient;
import com.metabroadcast.applications.client.model.internal.Application;
import com.metabroadcast.applications.client.model.internal.Environment;
import com.metabroadcast.applications.client.query.Query;
import com.metabroadcast.applications.client.query.Result;
import com.metabroadcast.common.properties.Configurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApiKeyApplicationFetcher implements ApplicationFetcher {

    public static final String API_KEY_QUERY_PARAMETER = "apiKey";

    private static final String APP_CLIENT_ENV = checkNotNull(Configurer.get("applications.client.env").get());
    private static final Logger log = LoggerFactory.getLogger(ApiKeyApplicationFetcher.class);

    private final Environment environment;
    
    private final ApplicationsClient applicationsClient;

    @VisibleForTesting
    ApiKeyApplicationFetcher(ApplicationsClient applicationsClient, Environment environment) {
        this.applicationsClient = checkNotNull(applicationsClient);
        this.environment = checkNotNull(environment);
    }

    public static ApiKeyApplicationFetcher create(ApplicationsClient applicationsClient) {
        return new ApiKeyApplicationFetcher(
                applicationsClient,
                Environment.parse(APP_CLIENT_ENV)
        );
    }

    @Override
    public Optional<Application> applicationFor(HttpServletRequest request)
            throws InvalidApiKeyException {
        String apiKey;
        try {
            apiKey = Objects.firstNonNull(
                    request.getParameter(API_KEY_QUERY_PARAMETER),
                    request.getHeader(API_KEY_QUERY_PARAMETER)
            );
        } catch (NullPointerException e) {
            log.info("No api key from request: {}", request.getRequestURI(), e);
            return Optional.empty();
        }

        Result result = applicationsClient.resolve(Query.create(apiKey, environment));

        if (result.getErrorCode().isPresent()) {
            throw InvalidApiKeyException.create(apiKey, result.getErrorCode().get());
        }
        return result.getSingleResult();
    }
}
