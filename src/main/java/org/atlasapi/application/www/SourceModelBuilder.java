package org.atlasapi.application.www;

import java.math.BigInteger;

import org.atlasapi.media.entity.Publisher;

import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class SourceModelBuilder implements ModelBuilder<Publisher> {

    private final NumberToShortStringCodec idCodec;

    public SourceModelBuilder(NumberToShortStringCodec idCodec) {
        this.idCodec = idCodec;
    }

    @Override
    public SimpleModel build(Publisher target) {
        SimpleModel model = new SimpleModel();

        model.put("id", idCodec.encode(BigInteger.valueOf(1000l + target.ordinal())));
        model.put("key", target.key());
        model.put("title", target.title());
        model.put("country", new SimpleModel().put("code", target.country().code()).put("name", target.country().getName()));
        return model;
    }

}
