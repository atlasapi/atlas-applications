package org.atlasapi.application.sources;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.DBCollection;


public class MongoSourceRequestStore implements SourceRequestStore {

public static final String SOURCE_REQUESTS_COLLECTION = "sourceRequests";
    
    private final SourceRequestTranslator translator = new SourceRequestTranslator();
    
    private final DBCollection sourceRequests;
    
    public MongoSourceRequestStore(DatabasedMongo mongo) {
        this.sourceRequests = mongo.collection(SOURCE_REQUESTS_COLLECTION);
    }
    @Override
    public void store(SourceRequest sourceRequest) {
        this.sourceRequests.save(translator.toDBObject(sourceRequest));
    }

}
