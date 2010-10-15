package org.atlasapi.application.persistence;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationCredentials;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.metabroadcast.common.net.IpRange;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class CachingMongoApplicationStore implements ApplicationStore {

	private Map<String, Application> slugMap;
	private Map<String, Application> keyMap;
	private Map<InetAddress, Application> addressMap;

	private final ApplicationStore store;
	
	public CachingMongoApplicationStore(ApplicationStore store) {
		this.store = store;
		initCache(store.applications());
	}
	
	
	private void initCache(Iterable<Application> applications) {
		slugMap = new MapMaker().makeMap();
		keyMap = new MapMaker().makeMap();
		addressMap = new MapMaker().makeMap();;
		for (Application application : applications) {
			updateCache(application);
		}
	}

	private void updateCache(Application application) {
		slugMap.put(application.getSlug(), application);
		ApplicationCredentials credentials = application.getCredentials();
		if (credentials != null) {
			if (credentials.getApiKey() != null){
				keyMap.put(credentials.getApiKey(), application);
			}
			for (IpRange range : credentials.getIpAddressRanges()) {
				for (InetAddress address : range.asList()) {
					addressMap.put(address, application);
				}
			}
		}
	}

	@Override
	public Set<Application> applications() {
		return ImmutableSet.copyOf(slugMap.values());
	}
	
	@Override
	public Application applicationFor(String slug) {
		return slugMap.get(slug);
	}
	
	@Override
	public Application applicationForKey(String key) {
		return keyMap.get(key);
	}

	@Override
	public Application applicationForAddress(InetAddress address) {
		return addressMap.get(address);
	}

	@Override
	public void persist(Application application) {
		store.persist(application);
		updateCache(application);
	}
	
	@Override
	public void update(Application application) {
		store.update(application);
		updateCache(application);
	}
}
