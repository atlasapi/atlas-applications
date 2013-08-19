package org.atlasapi.application;

import java.util.Set;

import org.atlasapi.application.users.User;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;

public interface ApplicationStore {
    
    Iterable<Application> allApplications();

    Set<Application> applicationsFor(Optional<User> user);
    
    Set<Application> applicationsFor(Publisher source);
    
    Set<Application> writersFor(Publisher source);
    
    Optional<Application> applicationFor(String slug);
    
    Optional<Application> applicationForKey(String key);

    Application persist(Application application);
    
    Application update(Application application);
    
}
