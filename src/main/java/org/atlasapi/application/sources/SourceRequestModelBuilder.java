package org.atlasapi.application.sources;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationManager;
import org.atlasapi.media.entity.Publisher;

import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;


public class SourceRequestModelBuilder implements ModelBuilder<SourceRequest> {

    private final ApplicationManager appManager;
    private final ModelBuilder<Application> applicationModelBuilder;
    private final SourceIdCodec sourceIdCodec;
    
    public SourceRequestModelBuilder(ApplicationManager appManager, 
            ModelBuilder<Application> applicationModelBuilder,
            SourceIdCodec sourceIdCodec) {
        this.appManager = appManager;
        this.applicationModelBuilder = applicationModelBuilder;
        this.sourceIdCodec = sourceIdCodec;
    }
    
    @Override
    public SimpleModel build(SourceRequest target) {
        SimpleModel model = new SimpleModel();
        Application application = appManager.applicationFor(target.getAppSlug()).get();
        model.put("application",applicationModelBuilder.build(application));
        model.put("app_url", target.getAppUrl());
        model.put("email", target.getEmail());
        model.put("publisher", model(target.getPublisher()));
        model.put("reason", target.getReason());
        model.put("usage_type", model(target.getUsageType()));
        model.put("approved",target.isApproved());
        return model;
    }
    
    protected SimpleModel model(Publisher publisher) {
        return new SimpleModel()
            .put("id", sourceIdCodec.encode(publisher))
            .put("key", publisher.key())
            .put("title", publisher.title());
    }
    
    protected SimpleModel model(UsageType usageType) {
        return new SimpleModel()
            .put("key", usageType.toString())
            .put("title", usageType.title());
    }

}
