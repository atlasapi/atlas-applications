package org.atlasapi.application.users;

import java.math.BigInteger;

import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.application.users.v3.User;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class UserModelBuilder implements ModelBuilder<User> {

    private SubstitutionTableNumberCodec idCodec;
    private SourceIdCodec sourceIdCodec;

    public UserModelBuilder() {
        this.idCodec = new SubstitutionTableNumberCodec();
        this.sourceIdCodec = new SourceIdCodec();
    }
    
    @Override
    public SimpleModel build(User target) {
        return new SimpleModel()
            .put("id", idCodec.encode(BigInteger.valueOf(target.getId())))
            .put("role", target.getRole().toString().toLowerCase())
            .putStrings("apps", target.getApplications())
            .putStrings("sources", ImmutableSet.copyOf(Iterables.transform(target.getSources(), new Function<Publisher, String>() {
                @Override
                public String apply(Publisher input) {
                    return sourceIdCodec.encode(input);
                }
            })));
    }

}
