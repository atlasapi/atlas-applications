package org.atlasapi.application.persistence;

import org.atlasapi.application.model.Application;

public interface ApplicationPersistor {

	void persist(Application application);
	
}
