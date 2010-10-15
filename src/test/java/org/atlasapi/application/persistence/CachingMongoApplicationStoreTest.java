package org.atlasapi.application.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationCredentials;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.net.IpRange;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class CachingMongoApplicationStoreTest {

	DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();
	ApplicationTranslator translator = new ApplicationTranslator();
	
	CachingMongoApplicationStore store;
	private Application application1;
	private Application application2;
	
	@Before
	public void setUp() throws Exception {
		application1 = new Application("testSlug1");
		
		ApplicationCredentials credentials1 = new ApplicationCredentials();
		credentials1.setApiKey("testApiKey1");
		credentials1.setIpAddresses(ImmutableList.of(IpRange.fromString("192.168.0.0/24")));
		
		application1.setCredentials(credentials1);
		
		application2 = new Application("testSlug2");
		
		ApplicationCredentials credentials2 = new ApplicationCredentials();
		credentials2.setApiKey("testApiKey2");
		credentials2.setIpAddresses(ImmutableList.of(IpRange.fromString("10.0.0.0/24")));
		
		application2.setCredentials(credentials2);
		
		DBCollection appColl = mongo.collection("applications");
		appColl.insert(ImmutableList.copyOf(Iterables.transform(ImmutableList.of(application1,application2), new Function<Application, DBObject>(){
			@Override
			public DBObject apply(Application from) {
				return translator.toDBObject(from);
			}
		})));
		
		store = new CachingMongoApplicationStore(new MongoApplicationStore(mongo));
	}
	
	@Test
	public void testApplications() {
		assertThat(ImmutableSet.copyOf(store.applications()), equalTo(ImmutableSet.of(application1, application2)));
	}

	@Test
	public void testApplicationFor() {
		assertThat(store.applicationFor("testSlug1"), is(equalTo(application1)));
		assertThat(store.applicationFor("testSlug2"), is(equalTo(application2)));
		assertThat(store.applicationFor("snail"), nullValue());
	}

	@Test
	public void testApplicationForKey() {
		assertThat(store.applicationForKey("testApiKey1"), is(equalTo(application1)));
		assertThat(store.applicationForKey("testApiKey2"), is(equalTo(application2)));
		assertThat(store.applicationForKey("lock"), nullValue());
	}

	@Test
	public void testApplicationForAddress() throws UnknownHostException {
		assertThat(store.applicationForAddress(InetAddress.getByName("192.168.0.1")), is(equalTo(application1)));
		assertThat(store.applicationForAddress(InetAddress.getByName("10.0.0.5")), is(equalTo(application2)));
		assertThat(store.applicationForAddress(InetAddress.getByName("192.168.1.1")), nullValue());
	}

	@Test
	public void testPersist() throws UnknownHostException {
		Application application3 = new Application("testSlug3");
		
		ApplicationCredentials credentials3 = new ApplicationCredentials();
		credentials3.setApiKey("testApiKey3");
		credentials3.setIpAddresses(ImmutableList.of(IpRange.fromString("12.0.0.0/24")));
		
		application3.setCredentials(credentials3);
		
		store.persist(application3);
		
		assertThat(ImmutableSet.copyOf(store.applications()), equalTo(ImmutableSet.of(application1, application2, application3)));
		assertThat(store.applicationFor("testSlug3"), is(equalTo(application3)));
		assertThat(store.applicationForKey("testApiKey3"), is(equalTo(application3)));
		assertThat(store.applicationForAddress(InetAddress.getByName("12.0.0.5")), is(equalTo(application3)));
	}

	@Test
	public void testUpdate() throws UnknownHostException {
		
		application2 = new Application("testSlug2");
		
		ApplicationCredentials credentials2 = new ApplicationCredentials();
		credentials2.setApiKey("newTestApiKey2");
		credentials2.setIpAddresses(ImmutableList.of(IpRange.fromString("10.0.0.0/24"), IpRange.fromString("11.0.0.0/24")));
		
		application2.setCredentials(credentials2);
		
		store.update(application2);
		
		assertThat(store.applicationFor("testSlug2"), is(equalTo(application2)));
		assertThat(store.applicationForKey("newTestApiKey2"), is(equalTo(application2)));
		assertThat(store.applicationForAddress(InetAddress.getByName("11.0.0.5")), is(equalTo(application2)));
	}

}
