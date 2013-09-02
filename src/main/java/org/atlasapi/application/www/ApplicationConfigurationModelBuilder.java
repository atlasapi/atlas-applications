package org.atlasapi.application.www;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;

public class ApplicationConfigurationModelBuilder implements ModelBuilder<ApplicationConfiguration> {
	
	@Override
	public SimpleModel build(final ApplicationConfiguration target) {
		SimpleModel model = new SimpleModel();
		
		final Map<Publisher, SourceStatus> sourceStatuses = target.sourceStatuses();
		
		SimpleModelList publishers = new SimpleModelList();
		List<SourceStatus> statusOrder = ImmutableList.of(SourceStatus.AVAILABLE_ENABLED, 
		        SourceStatus.AVAILABLE_DISABLED, SourceStatus.REQUESTED, SourceStatus.REVOKED,
		        SourceStatus.UNAVAILABLE);
		
		for (SourceStatus sourceStatus : statusOrder) {
		    publishers.add(buildPublisherSection(sourceStatuses, sourceStatus));
		}
		//target.orderdPublishers();
		model.put("publishers", publishers);
		
		if (target.precedenceEnabled()) {
			model.put("precedence", true);
		}
		return model;
	}
	
	private String buildSectionKey(SourceStatus sourceStatus) {
	    String sourceKey = sourceStatus.getState().toString().toLowerCase();
	    sourceKey += "-";
	    sourceKey += sourceStatus.isEnabled() ? "enabled" : "disabled";
	    return sourceKey;
	}
	
	private String buildSectionTitle(SourceStatus sourceStatus) {
	    return sourceStatus.toString().substring(0, 1).toUpperCase() + sourceStatus.toString().substring(1);
	}
	
	private SimpleModel buildPublisherSection(final Map<Publisher, SourceStatus> sourceStatuses, final SourceStatus sourceStatus) {
	    SimpleModel sectionModel = new SimpleModel();
	    sectionModel.put("section_key", buildSectionKey(sourceStatus));
	    sectionModel.put("section_title", buildSectionTitle(sourceStatus));
	    sectionModel.put("precedence_sortable", sourceStatus.equals(SourceStatus.AVAILABLE_ENABLED));
	    
	    final Iterable<Publisher> statuses = Iterables.filter(sourceStatuses.keySet(), new Predicate<Publisher>() {
          @Override
            public boolean apply(@Nullable Publisher input) {
                return sourceStatuses.get(input).equals(sourceStatus);
            }
        
        });
	    
	    List<SimpleModel> publishers = ImmutableList.copyOf(Iterables.transform(statuses, new Function<Publisher, SimpleModel>(){
            @Override
            public SimpleModel apply(Publisher publisher) {
                return model(publisher, sourceStatuses.get(publisher));
            }

        }));
	    sectionModel.put("publishers", publishers);
	    return sectionModel;
	}
	
	

    protected SimpleModel model(Publisher publisher, SourceStatus sourceStatus) {
        return new SimpleModel()
            .put("key", publisher.key())
            .put("title", publisher.title())
            .put("state",sourceStatus.getState().toString().toLowerCase())
            .put("enabled", sourceStatus.isEnabled());
    }
}
