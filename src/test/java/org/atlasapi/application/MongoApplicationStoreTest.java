package org.atlasapi.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.ApplicationCredentials;
import org.atlasapi.application.MongoApplicationStore;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.media.content.Publisher;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.net.IpRange;
import com.metabroadcast.common.persistence.MongoTestHelper;

public class MongoApplicationStoreTest {

    private final ApplicationCredentials creds = new ApplicationCredentials("apiKey");
	private final MongoApplicationStore appStore = new MongoApplicationStore(MongoTestHelper.anEmptyTestDatabase());
	
	@Test
	public void testApplicationPersists() {
		Application app1 = Application.application("test1").withTitle("Test 1").withCredentials(creds).build();

		appStore.persist(app1);

		Optional<Application> retrieved = appStore.applicationFor("test1");

		assertEquals(app1.getSlug(), retrieved.get().getSlug());
		assertEquals(app1.getTitle(), retrieved.get().getTitle());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPersitenceOfApplicationsWithSameSlugsFails() {
	    Application app1 = Application.application("testa").withTitle("Test 1").withCredentials(creds).build();
	    Application app2 = Application.application("testa").withTitle("Test 2").withCredentials(creds).build();

	    appStore.persist(app1);
		appStore.persist(app2);

		Optional<Application> retrieved = appStore.applicationFor("test1");

		assertEquals(app1.getSlug(), retrieved.get().getSlug());
		assertEquals(app1.getTitle(), retrieved.get().getTitle());
	}
	
	@Test
	public void testConfigurationPublisherPersistence() {
	    Application app1 = Application.application("testb").withTitle("Test 1").withCredentials(creds).build();
		
		ApplicationConfiguration config = new ApplicationConfiguration(ImmutableMap.of(Publisher.FIVE, SourceStatus.AVAILABLE_ENABLED), null);
		
		app1 = app1.copy().withConfiguration(config).build();
		
		appStore.persist(app1);
		
		Optional<Application> retrieved = appStore.applicationFor("testb");

		assertTrue(retrieved.get().getConfiguration().getEnabledSources().contains(Publisher.FIVE));
	}
	
	@Test
	public void testCredentialAPIKeyPersistence() {
	    Application app1 = Application.application("testc").withTitle("Test 1").withCredentials(creds).build();
		
		appStore.persist(app1);
		
		Optional<Application> retrieved = appStore.applicationFor("testc");
		
		assertEquals(app1.getCredentials().getApiKey(), retrieved.get().getCredentials().getApiKey());
	}
	
	@Test
	public void testCredentialIPAddressPersistence() throws UnknownHostException {
	    Application app1 = Application.application("testd").withTitle("Test 1").withCredentials(creds).build();
		
		ApplicationCredentials credentials = app1.getCredentials().copyWithIpAddresses(ImmutableList.of(new IpRange(InetAddress.getLocalHost())));
		
		app1 = app1.copy().withCredentials(credentials).build();
		
		appStore.persist(app1);
		
		Optional<Application> retrieved = appStore.applicationFor("testd");
		
		assertEquals(app1.getCredentials().getIpAddressRanges(), retrieved.get().getCredentials().getIpAddressRanges());
	}
	
	@Test
	public void testGetApplicationByAPIKey() {
	    Application app1 = Application.application("teste").withTitle("Test 1").withCredentials(creds).build();
		
		appStore.persist(app1);
		
		Optional<Application> retrieved = appStore.applicationForKey(app1.getCredentials().getApiKey());
		
		assertEquals(app1, retrieved.get());
	}
}
