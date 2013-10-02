package org.atlasapi.application;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.atlasapi.application.users.User;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CacheBackedApplicationStore implements OldApplicationStore {

    private final OldApplicationStore delegate;
    private LoadingCache<String, Optional<OldApplication>> slugCache;
    private LoadingCache<String, Optional<OldApplication>> keyCache;

    public CacheBackedApplicationStore(final OldApplicationStore delegate) {
        this.delegate = delegate;
        this.slugCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Optional<OldApplication>>() {
                    @Override
                    public Optional<OldApplication> load(String key) throws Exception {
                        return delegate.applicationFor(key);
                    }
                });
        this.keyCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Optional<OldApplication>>() {
                    @Override
                    public Optional<OldApplication> load(String key) throws Exception {
                        return delegate.applicationForKey(key);
                    }
                });
    }

    @Override
    public Set<OldApplication> applicationsFor(Optional<User> user) {
        return delegate.applicationsFor(user);
    }

    @Override
    public Optional<OldApplication> applicationFor(String slug) {
        return slugCache.getUnchecked(slug);
    }

    @Override
    public Optional<OldApplication> applicationForKey(String key) {
        return keyCache.getUnchecked(key);
    }
    
    @Override
    public OldApplication persist(OldApplication application) {
        OldApplication delegated = delegate.persist(application);
        slugCache.invalidate(application.getSlug());
        keyCache.invalidate(application.getCredentials().getApiKey());
        return delegated;
    }

    @Override
    public OldApplication update(OldApplication application) {
        OldApplication delegated = delegate.update(application);
        slugCache.invalidate(application.getSlug());
        keyCache.invalidate(application.getCredentials().getApiKey());
        return delegated;
    }

    @Override
    public Set<OldApplication> applicationsFor(Publisher source) {
        return delegate.applicationsFor(source);
    }

    @Override
    public Iterable<OldApplication> allApplications() {
        return delegate.allApplications();
    }

}
