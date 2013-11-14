package org.atlasapi.application.sources;

import org.atlasapi.application.v3.Application;
import org.atlasapi.media.entity.Publisher;

import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class SourceRequestTranslator {
    public static final String APPSLUG_KEY = "appSlug";
    public static final String PUBLISHER_KEY = "publisher";
    public static final String USAGE_TYPE_KEY = "usageType";
    public static final String EMAIL_KEY = "email";
    public static final String APPURL_KEY = "appUrl";
    public static final String REASON_KEY = "reason";
    public static final String APPROVED_KEY = "approved";
    
    public String createKey(SourceRequest sourceRequest) {
        return String.format("%s|%s", sourceRequest.getAppSlug(), sourceRequest.getPublisher().key());
    }
    
    public String createKey(Application application, Publisher publisher) {
        return String.format("%s|%s", application.getSlug(), publisher.key());
    }
    
    public DBObject toDBObject(SourceRequest sourceRequest) {
        DBObject dbo = new BasicDBObject();
        TranslatorUtils.from(dbo, MongoConstants.ID, createKey(sourceRequest));
        TranslatorUtils.from(dbo, APPSLUG_KEY, sourceRequest.getAppSlug());
        TranslatorUtils.from(dbo, PUBLISHER_KEY, sourceRequest.getPublisher().key());
        TranslatorUtils.from(dbo, USAGE_TYPE_KEY, sourceRequest.getUsageType().toString());
        TranslatorUtils.from(dbo, EMAIL_KEY, sourceRequest.getEmail());
        TranslatorUtils.from(dbo, APPURL_KEY, sourceRequest.getAppUrl());
        TranslatorUtils.from(dbo, REASON_KEY, sourceRequest.getReason());
        TranslatorUtils.from(dbo, APPROVED_KEY, sourceRequest.isApproved());
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
                .withApproved(TranslatorUtils.toBoolean(dbo, APPROVED_KEY))
                .build();
    }
}
