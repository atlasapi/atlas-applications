package org.atlasapi.application.sources;

import org.atlasapi.media.entity.Publisher;

import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class SourceRequestTranslator {
    private static final String APPSLUG_KEY = "appSlug";
    private static final String PUBLISHER_KEY = "publisher";
    private static final String USAGE_TYPE_KEY = "usageType";
    private static final String EMAIL_KEY = "email";
    private static final String APPURL_KEY = "appUrl";
    private static final String REASON_KEY = "reason";
    
    public DBObject toDBObject(SourceRequest sourceRequest) {
        DBObject dbo = new BasicDBObject();
        TranslatorUtils.from(dbo, APPSLUG_KEY, sourceRequest.getAppSlug());
        TranslatorUtils.from(dbo, PUBLISHER_KEY, sourceRequest.getPublisher().key());
        TranslatorUtils.from(dbo, USAGE_TYPE_KEY, sourceRequest.getUsageType().toString());
        TranslatorUtils.from(dbo, EMAIL_KEY, sourceRequest.getEmail());
        TranslatorUtils.from(dbo, APPURL_KEY, sourceRequest.getAppUrl());
        TranslatorUtils.from(dbo, REASON_KEY, sourceRequest.getReason());
        return dbo;
    }
    
    public SourceRequest fromDBObject(DBObject dbo) {
        if (dbo == null) {
            return null;
        }
        return SourceRequest.builder()
                .withAppSlug(TranslatorUtils.toString(dbo, APPSLUG_KEY))
                .withPublisher(Publisher.fromKey(TranslatorUtils.toString(dbo, PUBLISHER_KEY)).requireValue())
                .withUsageType(UsageType.valueOf(TranslatorUtils.toString(dbo, USAGE_TYPE_KEY)))
                .withEmail(TranslatorUtils.toString(dbo, EMAIL_KEY))
                .withAppUrl(TranslatorUtils.toString(dbo, APPURL_KEY))
                .withReason(TranslatorUtils.toString(dbo, REASON_KEY))
                .build();
    }
}
