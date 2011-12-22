package org.atlasapi.application.sources;

import org.atlasapi.media.entity.Publisher;

import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class SourceModelBuilder implements ModelBuilder<Publisher> {

    private final SourceIdCodec idCodec;

    public SourceModelBuilder(SourceIdCodec idCodec) {
        this.idCodec = idCodec;
    }

    @Override
    public SimpleModel build(Publisher target) {
        SimpleModel model = new SimpleModel();

        model.put("id", idCodec.encode(target));
        model.put("key", target.key());
        model.put("title", target.title());
        model.put("country", new SimpleModel().put("code", target.country().code()).put("name", target.country().getName()));
        return model;
    }

}
