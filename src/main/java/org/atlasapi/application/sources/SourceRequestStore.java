package org.atlasapi.application.sources;

import java.util.Set;

import org.atlasapi.application.Application;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;


public interface SourceRequestStore {
    
    void store(SourceRequest sourceRequest);
    
    Optional<SourceRequest> getBy(Application application, Publisher publisher);
    
    Set<SourceRequest> sourceRequestsFor(Publisher publisher);

}
