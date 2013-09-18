package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.OldApplicationConfiguration;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public interface ApplicationConfigurationFetcher {
	
	Maybe<OldApplicationConfiguration> configurationFor(HttpServletRequest request);

    ImmutableSet<String> getParameterNames();

}
