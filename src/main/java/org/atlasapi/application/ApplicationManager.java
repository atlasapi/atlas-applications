package org.atlasapi.application;

import static org.atlasapi.application.OldApplication.application;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.atlasapi.application.users.User;
import org.atlasapi.application.users.UserStore;
import org.atlasapi.application.www.PublisherConfiguration;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.net.IpRange;
import com.metabroadcast.common.time.DateTimeZones;

public class ApplicationManager implements OldApplicationStore {

    private final OldApplicationStore delegate;
    private final UserStore userStore;

    public ApplicationManager(OldApplicationStore delegate, UserStore userStore) {
        this.delegate = delegate;
        this.userStore = userStore;
    }
    
    public void createNewApplication(User user, String slug, String title) {
        Preconditions.checkNotNull(user, "Unknown User");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title) && slug.matches("[a-z0-9][a-z0-9\\-]{1,255}"), "Invalid application slug");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "Invalid application title");
        Preconditions.checkState(!applicationFor(slug).isPresent(), "Application already exists");
        
        OldApplication application = application(slug)
                .withTitle(title)
                .withDescription(null)
                .createdAt(new DateTime(DateTimeZones.UTC))
                .withCredentials(new OldApplicationCredentials(UUID.randomUUID().toString().replaceAll("-", "")))
                .withConfiguration(OldApplicationConfiguration.DEFAULT_CONFIGURATION).build();
        
        persist(application);
        user.addApplication(application);
        userStore.store(user);
    }
    
    public OldApplication requestPublisher(String slug, Publisher publisher) {
        Preconditions.checkNotNull(publisher);
        
        OldApplication app = applicationForSlug(slug);
        
        return update(app.copy().withConfiguration(app.getConfiguration().request(publisher)).build());
    }

    public OldApplication enablePublisher(String slug, Publisher publisher) {
        Preconditions.checkNotNull(publisher);
        
        OldApplication app = applicationForSlug(slug);
        
        return update(app.copy().withConfiguration(app.getConfiguration().enable(publisher)).build());
        
    }

    public OldApplication disablePublisher(String slug, Publisher publisher) {
        Preconditions.checkNotNull(publisher);
        
        OldApplication app = applicationForSlug(slug);
        
        return update(app.copy().withConfiguration(app.getConfiguration().disable(publisher)).build());
        
    }
    
    public OldApplication setPublisherConfiguration(String slug, PublisherConfiguration configuration) {
    	OldApplication app = applicationForSlug(slug);
    	OldApplicationConfiguration appConfiguration = app.getConfiguration();
    	for (Publisher source : configuration.getEnabled()) {
    		appConfiguration = appConfiguration.enable(source);
    	}
    	for (Publisher source : configuration.getDisabled()) {
    		appConfiguration = appConfiguration.disable(source);
    	}
    	return update(app.copy().withConfiguration(appConfiguration).build());
    }

    public OldApplication approvePublisher(String slug, Publisher publisher) {
        Preconditions.checkNotNull(publisher);
        
        OldApplication app = applicationForSlug(slug);
        
        return update(app.copy().withConfiguration(app.getConfiguration().approve(publisher)).build());
    }

    public OldApplication setSourcePrecedence(String slug, List<Publisher> publishers) {
        OldApplication app = applicationForSlug(slug);
        if (publishers == null) {
        	app = app.copy().withConfiguration(app.getConfiguration().copyWithNullPrecedence()).build();
        }
        else {
            app = app.copy().withConfiguration(app.getConfiguration().copyWithPrecedence(publishers)).build();
        }
        update(app);
        return app;
    }

    public OldApplication addIpRange(String slug, IpRange range) {
        OldApplication app = applicationForSlug(slug);

        Preconditions.checkArgument(range != null, "Invalid IP range");

        Set<IpRange> currentIps = app.getCredentials().getIpAddressRanges();
        app = app.copy().withCredentials(app.getCredentials().copyWithIpAddresses(Iterables.concat(currentIps, ImmutableList.of(range)))).build();
        update(app);
        
        return app;
    }

    public OldApplication removeIpRange(String slug, IpRange range) {
        OldApplication app = applicationForSlug(slug);
        
        Preconditions.checkNotNull(range);

        Set<IpRange> currentRanges = app.getCredentials().getIpAddressRanges();
        
        Preconditions.checkState(currentRanges.contains(range));
        
        Set<IpRange> newIps = Sets.newHashSet(currentRanges);
        newIps.remove(range);
        app = app.copy().withCredentials(app.getCredentials().copyWithIpAddresses(newIps)).build();
        
        update(app);
        return app;
    }

    private OldApplication applicationForSlug(String slug) {
        Optional<OldApplication> possibleApp = applicationFor(slug);
        Preconditions.checkState(possibleApp.isPresent(), "Unknown application " + slug);
        
        return possibleApp.get();
    }
    
    public Set<OldApplication> applicationsFor(Optional<User> possibleUser) {
        return delegate.applicationsFor(possibleUser);
    }

    @Override
    public Optional<OldApplication> applicationFor(String slug) {
        return delegate.applicationFor(slug);
    }

    @Override
    public Optional<OldApplication> applicationForKey(String key) {
        return delegate.applicationForKey(key);
    }

    @Override
    public OldApplication persist(OldApplication application) {
        return delegate.persist(application);
    }

    @Override
    public OldApplication update(OldApplication application) {
        return delegate.update(application);
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
