package org.atlasapi.application.query;

import com.metabroadcast.applications.client.ApplicationsClient;
import com.metabroadcast.applications.client.exceptions.ErrorCode;
import com.metabroadcast.applications.client.model.internal.Application;
import com.metabroadcast.applications.client.model.internal.Environment;
import com.metabroadcast.applications.client.query.Query;
import com.metabroadcast.applications.client.query.Result;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationFetcherTest {

    private ApplicationFetcher applicationFetcher;
    private final String validApiKey = "validKey";
    private final String invalidApiKey = "invalidKey";
    private final Environment environment = Environment.STAGE;

    @Mock
    private ApplicationsClient applicationsClient = mock(ApplicationsClient.class);

    @Mock
    private HttpServletRequest request = mock(HttpServletRequest.class);

    @Before
    public void setUp() {

        when(applicationsClient.resolve(Query.create(validApiKey, environment)))
                .thenReturn(Result.success(mock(Application.class)));
        when(applicationsClient.resolve(Query.create(invalidApiKey, environment)))
                .thenReturn(Result.failure(ErrorCode.NOT_FOUND));

        applicationFetcher = new ApiKeyApplicationFetcher(applicationsClient, environment);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void invalidApiKeyExceptionOnInvalidKey() throws Exception {
        thrown.expect(InvalidApiKeyException.class);

        when(request.getParameter(ApiKeyApplicationFetcher.API_KEY_QUERY_PARAMETER))
                .thenReturn(invalidApiKey);

        applicationFetcher.applicationFor(request);
    }

    @Test
    public void noExceptionThrownOnValidApiKey() throws Exception {

        when(request.getParameter(ApiKeyApplicationFetcher.API_KEY_QUERY_PARAMETER))
                .thenReturn(validApiKey);

        Optional<Application> application = applicationFetcher.applicationFor(request);

        assert(application.isPresent());
    }

}
