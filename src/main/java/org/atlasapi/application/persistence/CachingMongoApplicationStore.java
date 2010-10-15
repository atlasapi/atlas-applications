package org.atlasapi.application.persistence;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationCredentials;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.net.IpRange;

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
		addressMap = new MapMaker().makeMap();
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
	public void update(final Application application) {
		checkForRangesClashes(application);
		store.update(application);
		
		Map<InetAddress, Application> tempAddressMap = new MapMaker().makeMap();
		tempAddressMap.putAll(Maps.filterValues(addressMap, new Predicate<Application>(){
			@Override
			public boolean apply(Application input) {
				return !input.equals(application);
			}
		}));
		addressMap = tempAddressMap;
		updateCache(application);
	}
	
	private void checkForRangesClashes(Application application) {
		ApplicationCredentials credentials = application.getCredentials();
		if (credentials != null) {
			for (IpRange range : credentials.getIpAddressRanges()) {
				for (Application app : applications()) {
					if(!application.equals(app)) {
						ApplicationCredentials cre = app.getCredentials();
						if (cre != null) {
							for (IpRange ran : cre.getIpAddressRanges()) {
								if (Sets.intersection(ImmutableSet.copyOf(range.asList()), ImmutableSet.copyOf(ran.asList())).size() > 0) {
									throw new IllegalArgumentException("IP Range Clash: designated IP Range is already assigned to application " + app);
								}
							}
						}
					}
				}
			}
		}
	}
}
