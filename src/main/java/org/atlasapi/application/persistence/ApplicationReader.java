package org.atlasapi.application.persistence;

import java.util.Set;

import org.atlasapi.application.model.Application;

public interface ApplicationReader {

	Application applicationFor(String slug);
	Set<Application> applications();
	
}
