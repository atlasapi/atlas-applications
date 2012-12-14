package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;

import com.metabroadcast.common.base.Maybe;

public interface ApplicationConfigurationFetcher {
	
	Maybe<ApplicationConfiguration> configurationFor(HttpServletRequest request) throws InvalidAPIKeyException;

}
