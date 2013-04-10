package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationConfiguration;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public interface ApplicationConfigurationFetcher {
	
	Maybe<ApplicationConfiguration> configurationFor(HttpServletRequest request);

    ImmutableSet<String> getParameterNames();

}
