package org.atlasapi.application.persistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.atlasapi.application.ApplicationCredentials;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ApplicationCredentialsTranslator {

	private static final Pattern ipAddressPattern = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");
	
	public static final String IP_ADDRESS_KEY = "ip_addresses";
	public static final String API_KEY_KEY = "api_key";

	public DBObject toDBObject(ApplicationCredentials credentials) {
		DBObject dbo = new BasicDBObject();

		TranslatorUtils.from(dbo, API_KEY_KEY, credentials.getApiKey());
		TranslatorUtils.fromList(dbo, ImmutableList.copyOf(Iterables.transform(credentials.getIpAddresses(), new Function<InetAddress, String>(){

			@Override
			public String apply(InetAddress address) {
				return address.getHostAddress();
			}
			
		})), IP_ADDRESS_KEY);
		
		return dbo;
	}
	
	public ApplicationCredentials fromDBObject(DBObject dbo) {
		ApplicationCredentials credentials = new ApplicationCredentials();
		
		if (dbo.containsField(API_KEY_KEY)) {
			credentials.setApiKey((String) dbo.get(API_KEY_KEY));
		}
		
		Predicate<String> matchesIPAdress = new Predicate<String>(){
			@Override
			public boolean apply(String address) {
				return ipAddressPattern.matcher(address).matches();
			}
		};
		
		Function<String, InetAddress> stringToInetAddress = new Function<String, InetAddress>(){
			@Override
			public InetAddress apply(String address) {
				try {
					return InetAddress.getByName(address);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return null;
				}
			}
		};
		
		credentials.setIpAddresses(Iterables.transform(Iterables.filter(TranslatorUtils.toList(dbo, IP_ADDRESS_KEY), matchesIPAdress), stringToInetAddress));
		
		return credentials;
	}
}
