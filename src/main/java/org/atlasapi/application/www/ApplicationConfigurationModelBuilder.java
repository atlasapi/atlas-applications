package org.atlasapi.application.www;

import org.atlasapi.application.ApplicationConfiguration;
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
		
		model.put("publishers", ImmutableList.copyOf(Iterables.transform(ImmutableList.copyOf(Publisher.values()), new Function<Publisher, SimpleModel>(){
			@Override
			public SimpleModel apply(Publisher publisher) {
				SimpleModel publisherModel = new SimpleModel();
				
				publisherModel.put("key", publisher.key());
				publisherModel.put("title", publisher.title());
				publisherModel.put("name", publisher.name());
				publisherModel.put("enabled", target.getIncludedPublishers().contains(publisher));
				
				return publisherModel;
			}
		})));
		
		return model;
	}

}
