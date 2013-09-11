package org.atlasapi.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.atlasapi.application.OldApplication;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.ApplicationCredentials;
import org.atlasapi.application.OldMongoApplicationStore;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.net.IpRange;
import com.metabroadcast.common.persistence.MongoTestHelper;

public class MongoApplicationStoreTest {

    private final ApplicationCredentials creds = new ApplicationCredentials("apiKey");
	private final OldMongoApplicationStore appStore = new OldMongoApplicationStore(MongoTestHelper.anEmptyTestDatabase());
	
	@Test
	public void testApplicationPersists() {
		OldApplication app1 = OldApplication.application("test1").withTitle("Test 1").withCredentials(creds).build();

		appStore.persist(app1);

		Optional<OldApplication> retrieved = appStore.applicationFor("test1");

		assertEquals(app1.getSlug(), retrieved.get().getSlug());
		assertEquals(app1.getTitle(), retrieved.get().getTitle());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPersitenceOfApplicationsWithSameSlugsFails() {
	    OldApplication app1 = OldApplication.application("testa").withTitle("Test 1").withCredentials(creds).build();
	    OldApplication app2 = OldApplication.application("testa").withTitle("Test 2").withCredentials(creds).build();

	    appStore.persist(app1);
		appStore.persist(app2);

		Optional<OldApplication> retrieved = appStore.applicationFor("test1");

		assertEquals(app1.getSlug(), retrieved.get().getSlug());
		assertEquals(app1.getTitle(), retrieved.get().getTitle());
	}
	
	@Test
	public void testConfigurationPublisherPersistence() {
	    OldApplication app1 = OldApplication.application("testb").withTitle("Test 1").withCredentials(creds).build();
		
		ApplicationConfiguration config = new ApplicationConfiguration(ImmutableMap.of(Publisher.FIVE, SourceStatus.AVAILABLE_ENABLED), null);
		
		app1 = app1.copy().withConfiguration(config).build();
		
		appStore.persist(app1);
		
		Optional<OldApplication> retrieved = appStore.applicationFor("testb");

		assertTrue(retrieved.get().getConfiguration().getEnabledSources().contains(Publisher.FIVE));
	}
	
	@Test
	public void testCredentialAPIKeyPersistence() {
	    OldApplication app1 = OldApplication.application("testc").withTitle("Test 1").withCredentials(creds).build();
		
		appStore.persist(app1);
		
		Optional<OldApplication> retrieved = appStore.applicationFor("testc");
		
		assertEquals(app1.getCredentials().getApiKey(), retrieved.get().getCredentials().getApiKey());
	}
	
	@Test
	public void testCredentialIPAddressPersistence() throws UnknownHostException {
	    OldApplication app1 = OldApplication.application("testd").withTitle("Test 1").withCredentials(creds).build();
		
		ApplicationCredentials credentials = app1.getCredentials().copyWithIpAddresses(ImmutableList.of(new IpRange(InetAddress.getLocalHost())));
		
		app1 = app1.copy().withCredentials(credentials).build();
		
		appStore.persist(app1);
		
		Optional<OldApplication> retrieved = appStore.applicationFor("testd");
		
		assertEquals(app1.getCredentials().getIpAddressRanges(), retrieved.get().getCredentials().getIpAddressRanges());
	}
	
	@Test
	public void testGetApplicationByAPIKey() {
	    OldApplication app1 = OldApplication.application("teste").withTitle("Test 1").withCredentials(creds).build();
		
		appStore.persist(app1);
		
		Optional<OldApplication> retrieved = appStore.applicationForKey(app1.getCredentials().getApiKey());
		
		assertEquals(app1, retrieved.get());
	}
}
