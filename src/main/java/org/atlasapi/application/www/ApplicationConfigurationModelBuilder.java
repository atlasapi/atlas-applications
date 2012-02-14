package org.atlasapi.application.www;

import java.util.Map;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class ApplicationConfigurationModelBuilder implements ModelBuilder<ApplicationConfiguration> {
	
	@Override
	public SimpleModel build(final ApplicationConfiguration target) {
		SimpleModel model = new SimpleModel();
		
		final Map<Publisher, SourceStatus> sourceStatuses = target.sourceStatuses();
		model.put("publishers", ImmutableList.copyOf(Iterables.transform(target.orderdPublishers(), new Function<Publisher, SimpleModel>(){
			@Override
			public SimpleModel apply(Publisher publisher) {
			    return model(publisher, sourceStatuses.get(publisher));
			}

		})));
		
		if (target.precedenceEnabled()) {
			model.put("precedence", true);
		}
		return model;
	}

    protected SimpleModel model(Publisher publisher, SourceStatus sourceStatus) {
        return new SimpleModel()
            .put("key", publisher.key())
            .put("title", publisher.title())
            .put("state",sourceStatus.getState().toString().toLowerCase())
            .put("enabled", sourceStatus.isEnabled());
    }
}
