package org.atlasapi.application.users.v3;

import com.google.common.base.Optional;
import com.metabroadcast.common.social.model.UserRef;

public interface UserStore {

    Optional<User> userForRef(UserRef ref);
    
    Optional<User> userForId(Long userId);
    
    Iterable<User> allUsers();
    
    void store(User user);
    
}
