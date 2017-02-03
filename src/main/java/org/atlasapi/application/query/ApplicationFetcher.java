package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;

import com.metabroadcast.applications.client.model.internal.Application;

import java.util.Optional;

public interface ApplicationFetcher {
	/**
	 * Attempts to get an Application for given credentials in a request, typically an API key
	 * @param request
	 * @return Application for the given credentials if one exists
	 * @throws InvalidApiKeyException If there was an error resolving the application
	 */
	Optional<Application> applicationFor(HttpServletRequest request) throws InvalidApiKeyException;

}
