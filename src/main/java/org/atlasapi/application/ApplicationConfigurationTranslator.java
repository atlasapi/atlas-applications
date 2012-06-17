package org.atlasapi.application;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.media.content.Publisher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ApplicationConfigurationTranslator {

	public static final String STATE_KEY = "state";
    public static final String PUBLISHER_KEY = "publisher";
    public static final String SOURCES_KEY = "sources";
	public static final String PRECEDENCE_KEY = "precedence";

	public DBObject toDBObject(ApplicationConfiguration configuration) {
		BasicDBObject dbo = new BasicDBObject();
		
		TranslatorUtils.from(dbo, SOURCES_KEY, sourceStatusesToList(configuration.sourceStatuses()));
		
		if (configuration.precedenceEnabled()) { 
			TranslatorUtils.fromList(dbo, Lists.transform(configuration.precedence(), Publisher.TO_KEY), PRECEDENCE_KEY);
		} else {
			dbo.put(PRECEDENCE_KEY, null);
		}
		return dbo;
	}
	
	private BasicDBList sourceStatusesToList(Map<Publisher, SourceStatus> sourceStatuses) {
	    BasicDBList statuses = new BasicDBList();
	    for (Entry<Publisher, SourceStatus> sourceStatus : sourceStatuses.entrySet()) {
            statuses.add(new BasicDBObject(ImmutableMap.of(
                    PUBLISHER_KEY, sourceStatus.getKey().key(), 
                    STATE_KEY, sourceStatus.getValue().getState().toString().toLowerCase(),
                    "enabled", sourceStatus.getValue().isEnabled()
            )));
        }
        return statuses;
    }
	
	public ApplicationConfiguration fromDBObject(DBObject dbo) {
	    Map<Publisher, SourceStatus> sourceStatuses = sourceStatusesFrom(TranslatorUtils.toDBObjectList(dbo, SOURCES_KEY));
	
		List<Publisher> precedence = dbo.get(PRECEDENCE_KEY) == null ? null : Lists.transform(TranslatorUtils.toList(dbo, PRECEDENCE_KEY), Publisher.FROM_KEY);

		return new ApplicationConfiguration(sourceStatuses, precedence);
	}
	
    private Map<Publisher, SourceStatus> sourceStatusesFrom(List<DBObject> list) {
        Builder<Publisher, SourceStatus> builder = ImmutableMap.builder();
        for (DBObject dbo : list) {
            builder.put(
                Publisher.fromKey(TranslatorUtils.toString(dbo, PUBLISHER_KEY)).requireValue(),
                sourceStatusFrom(dbo)
            );
        }
        return builder.build();
        
    }

    private SourceStatus sourceStatusFrom(DBObject dbo) {
        if (TranslatorUtils.toBoolean(dbo, "enabled")) {
            return SourceStatus.AVAILABLE_ENABLED;
        }
        switch (SourceState.valueOf(TranslatorUtils.toString(dbo, STATE_KEY).toUpperCase())) {
            case AVAILABLE:
                return SourceStatus.AVAILABLE_DISABLED;
            case REQUESTED:
                return SourceStatus.REQUESTED;
            case REVOKED:
                return SourceStatus.REVOKED;
            default:
                return SourceStatus.UNAVAILABLE;
        }
    }

}
