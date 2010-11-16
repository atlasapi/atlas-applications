package org.atlasapi.application.persistence;

import java.util.List;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ApplicationConfigurationTranslator {

	private static final String INCLUDED_PUBLISHERS_KEY = "includedPublishers";
	private static final String PRECEDENCE_KEY = "precedence";

	public DBObject toDBObject(ApplicationConfiguration configuration) {
		BasicDBObject dbo = new BasicDBObject();
		
		TranslatorUtils.fromSet(dbo, publisherKeySetOf(configuration.getIncludedPublishers()), INCLUDED_PUBLISHERS_KEY);
		
		if (configuration.precedenceEnabled()) { 
			TranslatorUtils.fromList(dbo, Lists.transform(configuration.precedence(), Publisher.TO_KEY), PRECEDENCE_KEY);
		} else {
			dbo.put(PRECEDENCE_KEY, null);
		}
		return dbo;
	}
	
	private Set<String> publisherKeySetOf(Set<Publisher> publishers) {
		return ImmutableSet.copyOf(Iterables.transform(publishers, Publisher.TO_KEY));
	}

	public ApplicationConfiguration fromDBObject(DBObject dbo) {
		Set<Publisher> included = publisherSetOf(TranslatorUtils.toSet(dbo, INCLUDED_PUBLISHERS_KEY));
	
		List<Publisher> precedence = null;
		
		if (dbo.get(PRECEDENCE_KEY) != null) {
			precedence = Lists.transform(TranslatorUtils.toList(dbo, PRECEDENCE_KEY), Publisher.FROM_KEY);
		}

		return new ApplicationConfiguration(included, precedence);
	}
	
	private Set<Publisher> publisherSetOf(Set<String> keys) {
		return ImmutableSet.copyOf(Iterables.transform(keys, new Function<String, Publisher>(){

			@Override
			public Publisher apply(String key) {
				Maybe<Publisher> publisher = Publisher.fromKey(key);
				return publisher.valueOrNull();
			}
			
		}));
	}

}
