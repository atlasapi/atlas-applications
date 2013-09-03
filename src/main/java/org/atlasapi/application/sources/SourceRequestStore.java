package org.atlasapi.application.sources;

import org.atlasapi.application.Application;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;


public interface SourceRequestStore {
    
    void store(SourceRequest sourceRequest);
    
    Optional<SourceRequest> getBy(Application application, Publisher publisher);

}
