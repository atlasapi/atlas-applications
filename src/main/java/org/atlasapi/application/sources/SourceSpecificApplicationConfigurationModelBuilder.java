package org.atlasapi.application.sources;

import org.atlasapi.application.OldApplicationConfiguration;
import org.atlasapi.application.www.ApplicationConfigurationModelBuilder;
import org.atlasapi.media.entity.Publisher;

import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;

public class SourceSpecificApplicationConfigurationModelBuilder extends ApplicationConfigurationModelBuilder {

    private final Publisher source;

    public SourceSpecificApplicationConfigurationModelBuilder(Publisher source) {
        this.source = source;
    }

    @Override
    public SimpleModel build(OldApplicationConfiguration target) {
        SimpleModel model = super.build(target);
        model.put("publishers", SimpleModelList.containing(model(source, target.sourceStatuses().get(source))));
        return model;
    }

}
