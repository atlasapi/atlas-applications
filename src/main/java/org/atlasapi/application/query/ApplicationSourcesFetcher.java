package org.atlasapi.application.query;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationSources;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public interface ApplicationSourcesFetcher {
	
	Optional<ApplicationSources> sourcesFor(HttpServletRequest request);

    ImmutableSet<String> getParameterNames();

}
