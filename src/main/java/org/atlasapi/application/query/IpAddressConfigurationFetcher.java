package org.atlasapi.application.query;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.persistence.ApplicationReader;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class IpAddressConfigurationFetcher implements ApplicationConfigurationFetcher {
	
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private static final Log log = LogFactory.getLog(IpAddressConfigurationFetcher.class);

	private final ApplicationReader reader;

	public IpAddressConfigurationFetcher(ApplicationReader reader) {
		this.reader = reader;
	}

	@Override
	public Maybe<ApplicationConfiguration> configurationFor(HttpServletRequest request) {
		
		String remoteAddr = getAddressFrom(request);
		
		try {
			InetAddress ipAddr = InetAddress.getByName(remoteAddr);
			
			Application app = reader.applicationForAddress(ipAddr);
			
			if (app != null) {
				return Maybe.fromPossibleNullValue(app.getConfiguration());
			}
			
		} catch (UnknownHostException e) {
			log.warn("UHE trying to create InetAddress from " + remoteAddr);
			return Maybe.nothing();
		}
		
		return Maybe.nothing();
	}

	private String getAddressFrom(HttpServletRequest request) {
		String forwardedFor = request.getHeader(X_FORWARDED_FOR);
		log.debug(X_FORWARDED_FOR + " : " + forwardedFor);
		if (!Strings.isNullOrEmpty(forwardedFor)) {
			List<String> forwardList = ImmutableList.copyOf(Splitter.on(",").trimResults().omitEmptyStrings().split(forwardedFor));
			if (!forwardList.isEmpty()){
				return forwardList.get(forwardList.size()-1);
			}
		}
		return request.getRemoteAddr();
	}

}
