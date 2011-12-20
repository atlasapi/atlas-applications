package org.atlasapi.application.persistence;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.atlasapi.application.Application;
import org.atlasapi.application.users.User;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class CacheBackedApplicationStore implements ApplicationStore {

    private final ApplicationStore delegate;
    private Cache<String, Optional<Application>> slugCache;
    private Cache<String, Optional<Application>> keyCache;

    public CacheBackedApplicationStore(final ApplicationStore delegate) {
        this.delegate = delegate;
        this.slugCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<String, Optional<Application>>() {

            @Override
            public Optional<Application> load(String key) throws Exception {
                return delegate.applicationFor(key);
            }
        });
        this.keyCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<String, Optional<Application>>() {

            @Override
            public Optional<Application> load(String key) throws Exception {
                return delegate.applicationForKey(key);
            }
        });
    }

    @Override
    public Set<Application> applicationsFor(Optional<User> user) {
        return delegate.applicationsFor(user);
    }

    @Override
    public Optional<Application> applicationFor(String slug) {
        return slugCache.getUnchecked(slug);
    }

    @Override
    public Optional<Application> applicationForKey(String key) {
        return keyCache.getUnchecked(key);
    }
    
    @Override
    public void persist(Application application) {
        delegate.persist(application);
        slugCache.invalidate(application.getSlug());
        keyCache.invalidate(application.getCredentials().getApiKey());
    }

    @Override
    public void update(Application application) {
        delegate.update(application);
        slugCache.invalidate(application.getSlug());
        keyCache.invalidate(application.getCredentials().getApiKey());
    }

}
