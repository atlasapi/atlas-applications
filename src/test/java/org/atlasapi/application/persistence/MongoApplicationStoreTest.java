package org.atlasapi.application.persistence;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.ApplicationConfiguration;
import org.atlasapi.application.model.ApplicationCredentials;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

public class MongoApplicationStoreTest {

	DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();

	MongoApplicationStore appStore = new MongoApplicationStore(mongo);

	@Test
	public void testApplicationPersists() {
		Application app1 = new Application("test1");
		app1.setTitle("Test 1");

		appStore.persist(app1);

		Application retrieved = appStore.applicationFor("test1");

		assertEquals(app1.getSlug(), retrieved.getSlug());
		assertEquals(app1.getTitle(), retrieved.getTitle());
	}

	@Test
	public void testPersitenceOfApplicationsWithSameSlugs() {
		Application app1 = new Application("test1");
		app1.setTitle("Application A");
		Application app2 = new Application("test1");
		app2.setTitle("Application B");

		appStore.persist(app1);
		appStore.persist(app2);

		Application retrieved = appStore.applicationFor("test1");

		assertEquals(app1.getSlug(), retrieved.getSlug());
		assertEquals(app1.getTitle(), retrieved.getTitle());
	}
	
	@Test
	public void testConfigurationPublisherPersistence() {
		Application app1 = new Application("test1");
		app1.setTitle("Application A");
		
		ApplicationConfiguration config = new ApplicationConfiguration();
		config.setIncludedPublishers(ImmutableSet.of(Publisher.BBC,Publisher.FIVE));
		config.setExcludedPublishers(ImmutableSet.of(Publisher.C4,Publisher.BLIP));
		
		app1.setConfiguration(config);
		
		appStore.persist(app1);
		
		Application retrieved = appStore.applicationFor("test1");

		assertEquals(2, retrieved.getConfiguration().getIncludedPublishers().size());
		assertEquals(2, retrieved.getConfiguration().getExcludedPublishers().size());
	}
	
	@Test
	public void testCredentialAPIKeyPersistence() {
		Application app1 = new Application("test1");
		
		ApplicationCredentials credentials = new ApplicationCredentials();
		credentials.setApiKey("I'm an API Key");
		
		app1.setCredentials(credentials);
		
		appStore.persist(app1);
		
		Application retrieved = appStore.applicationFor("test1");
		
		assertEquals(app1.getCredentials().getApiKey(), retrieved.getCredentials().getApiKey());
	}
	
	@Test
	public void testCredentialIPAddressPersistence() throws UnknownHostException {
		Application app1 = new Application("test1");
		
		ApplicationCredentials credentials = new ApplicationCredentials();
		credentials.setIpAddress(InetAddress.getLocalHost());
		
		app1.setCredentials(credentials);
		
		appStore.persist(app1);
		
		Application retrieved = appStore.applicationFor("test1");
		
		assertEquals(app1.getCredentials().getIpAddress(), retrieved.getCredentials().getIpAddress());
	}
}
