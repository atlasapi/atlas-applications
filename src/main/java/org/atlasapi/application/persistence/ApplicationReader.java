package org.atlasapi.application.persistence;

import java.net.InetAddress;
import java.util.Set;

import org.atlasapi.application.Application;

public interface ApplicationReader {

	Set<Application> applications();
	Application applicationFor(String slug);
	Application applicationForKey(String key);
	Application applicationForAddress(InetAddress address);
	
}
