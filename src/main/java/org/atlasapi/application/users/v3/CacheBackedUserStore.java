package org.atlasapi.application.users.v3;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.metabroadcast.common.social.model.UserRef;

public class CacheBackedUserStore implements UserStore {

    private final UserStore delegate;
    private LoadingCache<UserRef, Optional<User>> userRefCache;
    private LoadingCache<Long, Optional<User>> idCache;
    private LoadingCache<String, Set<User>> emailCache;

    public CacheBackedUserStore(final UserStore delegate) {
        this.delegate = delegate;
        this.userRefCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<UserRef, Optional<User>>() {
            @Override
            public Optional<User> load(UserRef key) throws Exception {
                return delegate.userForRef(key);
            }
        });
        this.idCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<Long, Optional<User>>() {
                    @Override
                    public Optional<User> load(Long key) throws Exception {
                        return delegate.userForId(key);
                    }
                });
        this.emailCache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Set<User>>() {
                    @Override
                    public Set<User> load(String email) throws Exception {
                        return delegate.userAccountsForEmail(email);
                    }
                });
    }
    
    @Override
    public Optional<User> userForRef(UserRef ref) {
        return userRefCache.getUnchecked(ref);
    }

    @Override
    public Set<User> userAccountsForEmail(String email) {
        return emailCache.getUnchecked(email);
    }

    @Override
    public Optional<User> userForId(Long userId) {
        return idCache.getUnchecked(userId);
    }

    @Override
    public void store(User user) {
        delegate.store(user);
        userRefCache.invalidate(user.getUserRef());
        idCache.invalidate(user.getId());
        emailCache.invalidate(user.getEmail());
    }

}
