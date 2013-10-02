package org.atlasapi.application;

import java.util.Set;

import org.atlasapi.application.users.User;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;

public interface OldApplicationStore {
    
    Iterable<OldApplication> allApplications();

    Set<OldApplication> applicationsFor(Optional<User> user);
    
    Set<OldApplication> applicationsFor(Publisher source);
    
    Optional<OldApplication> applicationFor(String slug);
    
    Optional<OldApplication> applicationForKey(String key);

    OldApplication persist(OldApplication application);
    
    OldApplication update(OldApplication application);
    
}
