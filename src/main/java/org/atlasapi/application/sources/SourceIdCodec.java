package org.atlasapi.application.sources;

import java.math.BigInteger;

import org.atlasapi.media.entity.Publisher;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

public class SourceIdCodec {
    
    private static final int ID_MAGNIFIER = 1000;
    private final NumberToShortStringCodec idCodec;
    
    public SourceIdCodec() {
        this(new SubstitutionTableNumberCodec());
    }

    public SourceIdCodec(NumberToShortStringCodec idCodec) {
        this.idCodec = idCodec;
    }

    public String encode(Publisher source) {
        return idCodec.encode(BigInteger.valueOf(ID_MAGNIFIER + source.ordinal()));
    }
    
    public Maybe<Publisher> decode(String id) {
        try {
            return Maybe.fromPossibleNullValue(Publisher.values()[idCodec.decode(id).intValue() - 1000]);
        } catch (Exception e) {
            return Maybe.nothing();
        }
    }
    
}
