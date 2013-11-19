package org.atlasapi.application.v3;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.atlasapi.application.users.v3.User;
import org.atlasapi.application.v3.Application;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CacheBackedApplicationStore implements ApplicationStore {

    private final ApplicationStore delegate;
    private LoadingCache<String, Optional<Application>> slugCache;
    private LoadingCache<String, Optional<Application>> keyCache;

    public CacheBackedApplicationStore(final ApplicationStore delegate) {
        this.delegate = delegate;
        this.slugCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, Optional<Application>>() {

            @Override
            public Optional<Application> load(String key) throws Exception {
                return delegate.applicationFor(key);
            }
        });
        this.keyCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, Optional<Application>>() {

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
    public Application persist(Application application) {
        Application delegated = delegate.persist(application);
        slugCache.invalidate(application.getSlug());
        keyCache.invalidate(application.getCredentials().getApiKey());
        return delegated;
    }

    @Override
    public Application update(Application application) {
        Application delegated = delegate.update(application);
        slugCache.invalidate(application.getSlug());
        keyCache.invalidate(application.getCredentials().getApiKey());
        return delegated;
    }

    @Override
    public Set<Application> applicationsFor(Publisher source) {
        return delegate.applicationsFor(source);
    }

    @Override
    public Iterable<Application> allApplications() {
        return delegate.allApplications();
    }

}
