package org.atlasapi.application.users;

import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.metabroadcast.common.social.model.translator.UserRefTranslator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class UserTranslator {
    
    private static final String APPS_KEY = "apps";
    private static final String USER_REF_KEY = "userRef";
    private static final String MANAGES_KEY = "manages";
    private static final String ROLE_KEY = "role";
    private static final String SCREEN_NAME_KEY = "screenName";
    private final UserRefTranslator userTranslator;

    public UserTranslator(UserRefTranslator userTranslator) {
        this.userTranslator = userTranslator;
    }
    
    public DBObject toDBObject(User user) {
        if (user == null) {
            return null;
        }
        
        BasicDBObject dbo = new BasicDBObject();
        
        TranslatorUtils.from(dbo, MongoConstants.ID, user.getId());
        TranslatorUtils.from(dbo, USER_REF_KEY, userTranslator.toDBObject(user.getUserRef()));
        TranslatorUtils.from(dbo, SCREEN_NAME_KEY, user.getScreenName());
        TranslatorUtils.from(dbo, APPS_KEY, user.getApplications());
        TranslatorUtils.from(dbo, MANAGES_KEY, Iterables.transform(user.getSources(), Publisher.TO_KEY));
        TranslatorUtils.from(dbo, ROLE_KEY, user.getRole().toString().toLowerCase());
        
        return dbo;
    }
    
    public User fromDBObject(DBObject dbo) {
        if (dbo == null) {
            return null;
        }

        User user = new User(TranslatorUtils.toLong(dbo, MongoConstants.ID));
        
        user.setUserRef(userTranslator.fromDBObject(TranslatorUtils.toDBObject(dbo, USER_REF_KEY)));
        if (dbo.containsField(SCREEN_NAME_KEY)) {
            user.setScreenName(TranslatorUtils.toString(dbo, SCREEN_NAME_KEY));
        }
        user.setApplications(TranslatorUtils.toSet(dbo, APPS_KEY));
        user.setSources(ImmutableSet.copyOf(Iterables.transform(TranslatorUtils.toSet(dbo, MANAGES_KEY),Publisher.FROM_KEY)));
        user.setRole(Role.valueOf(TranslatorUtils.toString(dbo, ROLE_KEY).toUpperCase()));
        
        return user;
    }
    
}
