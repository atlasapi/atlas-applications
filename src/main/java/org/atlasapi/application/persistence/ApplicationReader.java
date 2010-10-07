package org.atlasapi.application.persistence;

import org.atlasapi.application.model.Application;

public interface ApplicationReader {

	Application applicationFor(String slug);
	
}
