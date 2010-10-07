package org.atlasapi.application;

import org.atlasapi.application.www.ApplicationWebModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ApplicationWebModule.class})
public class ApplicationModule {
    
}
