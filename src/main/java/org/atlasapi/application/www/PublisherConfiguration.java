package org.atlasapi.application.www;
import java.util.Set;

import org.atlasapi.media.entity.Publisher;

public class PublisherConfiguration {
	private Set<Publisher> enabled;
	private Set<Publisher> disabled;
	
	public Set<Publisher> getEnabled() {
		return enabled;
	}
	public Set<Publisher> getDisabled() {
		return disabled;
	}
}
