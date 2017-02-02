package org.atlasapi.application.users.v3;

import java.util.Set;

import com.google.common.base.Optional;
import com.metabroadcast.common.social.model.UserRef;

public interface UserStore {

    Optional<User> userForRef(UserRef ref);

    Set<User> userAccountsForEmail(String email);
    
    Optional<User> userForId(Long userId);
    
    void store(User user);
    
}
