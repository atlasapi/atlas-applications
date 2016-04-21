package org.atlasapi.application.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.atlasapi.application.users.v3.MongoUserStore;
import org.atlasapi.application.users.v3.Role;
import org.atlasapi.application.users.v3.User;
import org.atlasapi.application.users.v3.UserStore;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.time.DateTimeZones;


public class MongoUserStoreTest {
    
    private UserStore store;

    private final Long id = Long.valueOf(5000);
    private final Long id2 = Long.valueOf(5000);
    private final UserRef userRef = new UserRef(5000, UserNamespace.TWITTER, "test");
    private final String screenName = "test123";
    private final String fullName = "Test One Two Three";
    private final String company = "the company";
    private final String email = "me@example.com";
    private final String website = "http://example.com";
    private final String profileImage = "http://example.com/image.png";
    private final Role role = Role.REGULAR;
    private final Set<String> applicationSlugs = ImmutableSet.of("app1", "app2");
    private final Set<Publisher> sources = ImmutableSet.of(Publisher.YOUTUBE, Publisher.SVERIGES_RADIO);

    private final boolean profileComplete = true;
    private final boolean profileDeactivated = true;
    private final DateTime licenseAccepted = DateTime.now(DateTimeZones.UTC);
    @Before
    public void setup() {
        store = new MongoUserStore(MongoTestHelper.anEmptyTestDatabase());

        User user = User.builder()
                .withId(id)
                .withUserRef(userRef)
                .withScreenName(screenName)
                .withFullName(fullName)
                .withCompany(company)
                .withEmail(email)
                .withWebsite(website)
                .withProfileImage(profileImage)
                .withApplicationSlugs(applicationSlugs)
                .withSources(sources)
                .withRole(role)
                .withProfileComplete(profileComplete)
                .withLicenseAccepted(licenseAccepted)
                .withProfileDeactivated(profileDeactivated)
                .build();
        User user2 = User.builder()
                .withId(id2)
                .withUserRef(userRef)
                .withScreenName(screenName)
                .withFullName(fullName)
                .withCompany(company)
                .withEmail(email)
                .withWebsite(website)
                .withProfileImage(profileImage)
                .withApplicationSlugs(applicationSlugs)
                .withSources(sources)
                .withRole(role)
                .withProfileComplete(profileComplete)
                .withLicenseAccepted(licenseAccepted)
                .withProfileDeactivated(profileDeactivated)
                .build();
        store.store(user);
        store.store(user2);
    }
    
    @Test 
    public void testUserPersists() {
        User retrieved = store.userForRef(userRef).get();
        assertEquals(id, retrieved.getId());
        assertEquals(userRef, retrieved.getUserRef());
        assertEquals(screenName, retrieved.getScreenName());
        assertEquals(company, retrieved.getCompany());
        assertEquals(email, retrieved.getEmail());
        assertEquals(website, retrieved.getWebsite());
        assertEquals(profileImage, retrieved.getProfileImage());
        assertEquals(role, retrieved.getRole());
        assertEquals(2, retrieved.getApplicationSlugs().size());
        assertTrue(retrieved.getApplicationSlugs().contains("app2"));
        assertEquals(2, retrieved.getSources().size());
        assertTrue(retrieved.getSources().contains(Publisher.YOUTUBE));
        assertTrue(retrieved.isProfileComplete());
        assertEquals(licenseAccepted, retrieved.getLicenseAccepted().get());
        assertTrue(retrieved.isProfileDeactivated());
    }

    @Test
    public void retrievesUserAccountsByEmail() {
        Set<User> retrievedByEmail = store.userAccountsForEmail(email);
        assertTrue(retrievedByEmail.size() == 2);
    }

}
