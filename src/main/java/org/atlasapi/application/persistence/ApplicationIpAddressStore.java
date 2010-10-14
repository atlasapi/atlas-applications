package org.atlasapi.application.persistence;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationCredentials;
import org.joda.time.Duration;

import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.caching.BackgroundComputingValue;
import com.metabroadcast.common.net.IpRange;

public class ApplicationIpAddressStore {

	private final Duration FIVE_MINUTES = new Duration(5 * 60 * 1000);
	private final BackgroundComputingValue<Map<InetAddress, Application>> computer;
	
	public ApplicationIpAddressStore(final ApplicationReader reader) {
		this.computer = new BackgroundComputingValue<Map<InetAddress,Application>>(FIVE_MINUTES, new Callable<Map<InetAddress,Application>>() {

			@Override
			public Map<InetAddress, Application> call() throws Exception {
				//foreach application, get credentials, for each ipRange, expand range, for each address in range, put in map with application.
				Map<InetAddress, Application> inetApplicationMap = Maps.newHashMap();
				
				for (Application application : reader.applications()) {
					ApplicationCredentials credentials = application.getCredentials();
					if (credentials == null) {
						continue;
					}
					Set<IpRange> ipRanges = credentials.getIpAddressRanges();
					if (ipRanges != null) {
						for (IpRange range : ipRanges) {
							for (InetAddress address : range.asList()) {
								inetApplicationMap.put(address, application);
							}
						}	
					}
				}
				
				return inetApplicationMap;
			}
		});
		this.computer.start(Maps.<InetAddress,Application>newHashMap());
	}
	
	public Maybe<Application> applicationFor(InetAddress address) {
		return Maybe.fromPossibleNullValue(computer.get().get(address));
	}
	
}
