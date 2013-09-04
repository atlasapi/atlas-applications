package org.atlas.application.notification;

import static org.atlas.application.notification.EmailNotificationSender.emailNotificationSender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.metabroadcast.common.webapp.soy.TemplateRenderer;

@Configuration
public class NotifierModule {

    private @Autowired TemplateRenderer soyRenderer;
    
    private @Value("${notifications.email.host}") String emailHost;
	private @Value("${notifications.email.username}") String emailUsername;
	private @Value("${notifications.email.password}") String emailPassword;
	private @Value("${notifications.email.from}") String from;
	private @Value("${notifications.email.fromFriendlyName}") String fromFriendlyName;
	private @Value("${notifications.email.to}") String to;
	
	@Bean public EmailNotificationSender emailSender() throws Exception {
		JavaMailSenderFactory factory = new JavaMailSenderFactory();
        factory.setHost(emailHost);
        factory.setUsername(emailUsername);
        factory.setPassword(emailPassword);
        
		return emailNotificationSender(factory.getObject(), soyRenderer)
				.withAdminToField(to)
				.withFromField(from)
				.withFriendlyFromName(fromFriendlyName)
				.build();
	}
	
}
