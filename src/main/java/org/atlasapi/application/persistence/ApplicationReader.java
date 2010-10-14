package org.atlasapi.application.persistence;

import java.net.InetAddress;
import java.util.Set;

import org.atlasapi.application.Application;

public interface ApplicationReader {

	Application applicationForKey(String key);
	Application applicationFor(String slug);
	Set<Application> applications();
	@Deprecated
	Application applicationForIpAddress(InetAddress address);
	
}
