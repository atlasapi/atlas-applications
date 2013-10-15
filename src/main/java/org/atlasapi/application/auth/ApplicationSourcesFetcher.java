package org.atlasapi.application.auth;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.ApplicationSources;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.social.model.UserRef;

public interface ApplicationSourcesFetcher {
	
	Optional<ApplicationSources> sourcesFor(HttpServletRequest request);
	
	Optional<UserRef> userFor(HttpServletRequest request);

    ImmutableSet<String> getParameterNames();

}
