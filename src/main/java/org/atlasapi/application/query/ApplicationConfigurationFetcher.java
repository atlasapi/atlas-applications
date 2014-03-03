package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.v3.ApplicationConfiguration;

import com.metabroadcast.common.base.Maybe;

public interface ApplicationConfigurationFetcher {
	/**
	 * Attempts to get an Application for given credentials in a request, typically an API key
	 * @param request
	 * @return Application for the given credentials if one exists
	 * @throws ApiKeyNotFoundException If an unknown API key was provided
	 * @throws RevokedApiKeyException If the API key has been blocked by an administrator
	 * @throws InvalidIpForApiKeyException If the IP address for the request is not whitelisted by the application
	 */
	Maybe<ApplicationConfiguration> configurationFor(HttpServletRequest request) throws ApiKeyNotFoundException, RevokedApiKeyException, InvalidIpForApiKeyException;

}
