package org.atlasapi.application.www;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationWebModule {
    @Bean public ApplicationController applicationController() {
        return new ApplicationController();
    }
}
