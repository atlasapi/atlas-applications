package org.atlasapi.application.persistence;

import java.util.Set;

import org.atlasapi.application.Application;
import org.atlasapi.application.users.User;

import com.google.common.base.Optional;

public interface ApplicationReader {

	Set<Application> applicationsFor(Optional<User> user);
	
	Optional<Application> applicationFor(String slug);
	
	Optional<Application> applicationForKey(String key);
	
}
