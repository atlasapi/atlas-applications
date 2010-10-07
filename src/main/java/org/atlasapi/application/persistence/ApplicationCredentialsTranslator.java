package org.atlasapi.application.persistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.atlasapi.application.model.ApplicationCredentials;

import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ApplicationCredentialsTranslator {

	private static final Pattern ipAddressPattern = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b");
	
	private static final String IP_ADDRESS_KEY = "ip_address";
	private static final String API_KEY_KEY = "api_key";

	public DBObject toDBObject(ApplicationCredentials credentials) {
		DBObject dbo = new BasicDBObject();

		TranslatorUtils.from(dbo, API_KEY_KEY, credentials.getApiKey());
		if (credentials.getIpAddress() != null) {
			TranslatorUtils.from(dbo, IP_ADDRESS_KEY, credentials.getIpAddress().getHostAddress());
		}
		
		return dbo;
	}
	
	public ApplicationCredentials fromDBObject(DBObject dbo) {
		ApplicationCredentials credentials = new ApplicationCredentials();
		
		if (dbo.containsField(API_KEY_KEY)) {
			credentials.setApiKey((String) dbo.get(API_KEY_KEY));
		}
		
		if (dbo.containsField(IP_ADDRESS_KEY)) {
			try {
				String hostAddress = (String) dbo.get(IP_ADDRESS_KEY);
				
				if (ipAddressPattern.matcher(hostAddress).matches()){
					credentials.setIpAddress(InetAddress.getByName(hostAddress));					
				}
				
			} catch (UnknownHostException e) {
				
			}
		}
		
		return credentials;
	}
}
