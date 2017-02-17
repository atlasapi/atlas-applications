package org.atlasapi.application;

import com.codahale.metrics.MetricRegistry;
import com.metabroadcast.applications.client.ApplicationsClient;
import com.metabroadcast.applications.client.ApplicationsClientImpl;
import com.metabroadcast.applications.client.model.internal.Environment;
import com.metabroadcast.common.properties.Configurer;
import org.atlasapi.application.query.ApplicationFetcher;
import org.atlasapi.application.query.ApiKeyApplicationFetcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import static com.google.common.base.Preconditions.checkNotNull;

@Configuration
@ImportResource("classpath:atlas-applications.xml")
public class ApplicationModule {

    private final String host = checkNotNull(Configurer.get("applications.client.host").get());
    private final String environment = checkNotNull(Configurer.get("applications.client.env").get());

	@Bean
	public ApplicationFetcher applicationFetcher() {
        return ApiKeyApplicationFetcher.create(applicationsClient(), applicationsEnvironment());
    }

    @Bean
    public ApplicationsClient applicationsClient() {
        return ApplicationsClientImpl.create(host , new MetricRegistry());
    }

    @Bean
    public Environment applicationsEnvironment() {
        return Environment.parse(environment);
    }
}
