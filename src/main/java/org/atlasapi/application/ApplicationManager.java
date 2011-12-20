package org.atlasapi.application;

import static org.atlasapi.application.Application.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.atlasapi.application.persistence.ApplicationStore;
import org.atlasapi.application.users.User;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.net.IpRange;

public class ApplicationManager implements ApplicationStore {

    private final ApplicationStore delegate;
    private final UserStore userStore;

    public ApplicationManager(ApplicationStore delegate, UserStore userStore) {
        this.delegate = delegate;
        this.userStore = userStore;
    }
    
    public void createNewApplication(User user, String slug, String title) {
        Preconditions.checkNotNull(user, "Unknown User");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title) && slug.matches("[a-z0-9][a-z0-9\\-]{1,255}"), "Invalid application slug");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "Invalid application title");
        Preconditions.checkState(!applicationFor(slug).isPresent(), "Application already exists");
        
        Application application = application(slug)
                .withTitle(title)
                .withCredentials(new ApplicationCredentials(UUID.randomUUID().toString().replaceAll("-", "")))
                .withConfiguration(ApplicationConfiguration.DEFAULT_CONFIGURATION).build();
        
        persist(application);
        user.addApplication(application);
        userStore.store(user);
        
    }

    public Application addPublisher(String slug, Publisher publisher) {
        Preconditions.checkNotNull(publisher);
        
        Application app = applicationForSlug(slug);
        
        Set<Publisher> currentPublishers = Sets.newHashSet(app.getConfiguration().getIncludedPublishers());
        currentPublishers.add(publisher);
        
        app = app.copy().withConfiguration(app.getConfiguration().copyWithIncludedPublishers(currentPublishers)).build();
        update(app);
        
        return app;
    }

    public Application removePublisher(String slug, Publisher publisher) {
        Preconditions.checkNotNull(publisher);
        
        Application app = applicationForSlug(slug);
        
        Set<Publisher> newPublishers = Sets.newHashSet(app.getConfiguration().getIncludedPublishers());
        newPublishers.remove(publisher);
        app = app.copy().withConfiguration(app.getConfiguration().copyWithIncludedPublishers(newPublishers)).build();
        update(app);
        
        return app;
    }

    public Application setSourcePrecedence(String slug, List<Publisher> publishers) {
        Application app = applicationForSlug(slug);
        
        app = app.copy().withConfiguration(app.getConfiguration().copyWithPrecedence(publishers)).build();
        
        update(app);
        return app;
    }

    public Application addIpRange(String slug, IpRange range) {
        Application app = applicationForSlug(slug);

        Preconditions.checkArgument(range != null, "Invalid IP range");

        Set<IpRange> currentIps = app.getCredentials().getIpAddressRanges();
        app = app.copy().withCredentials(app.getCredentials().copyWithIpAddresses(Iterables.concat(currentIps, ImmutableList.of(range)))).build();
        update(app);
        
        return app;
    }

    public Application removeIpRange(String slug, IpRange range) {
        Application app = applicationForSlug(slug);
        
        Preconditions.checkNotNull(range);

        Set<IpRange> currentRanges = app.getCredentials().getIpAddressRanges();
        
        Preconditions.checkState(currentRanges.contains(range));
        
        Set<IpRange> newIps = Sets.newHashSet(currentRanges);
        newIps.remove(range);
        app = app.copy().withCredentials(app.getCredentials().copyWithIpAddresses(newIps)).build();
        
        update(app);
        return app;
    }

    private Application applicationForSlug(String slug) {
        Optional<Application> possibleApp = applicationFor(slug);
        Preconditions.checkState(possibleApp.isPresent(), "Unknown application " + slug);
        
        return possibleApp.get();
    }
    
    public Set<Application> applicationsFor(Optional<User> possibleUser) {
        return delegate.applicationsFor(possibleUser);
    }

    @Override
    public Optional<Application> applicationFor(String slug) {
        return delegate.applicationFor(slug);
    }

    @Override
    public Optional<Application> applicationForKey(String key) {
        return delegate.applicationForKey(key);
    }

    @Override
    public void persist(Application application) {
        delegate.persist(application);
    }

    @Override
    public void update(Application application) {
        delegate.update(application);
    }
    
}
