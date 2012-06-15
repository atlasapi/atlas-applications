package org.atlas.application.notification;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.atlasapi.application.Application;
import org.atlasapi.media.content.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.google.common.base.Charsets;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.webapp.soy.TemplateRenderer;

public class EmailNotificationSender {
	private final JavaMailSender sender;

    private String from;
    private String fromFriendlyName;
    private String to;
    private final TemplateRenderer renderer;
    private static final String NOTIFICATION_TEMPLATE = "atlas.templates.applications.email.body";
    private static final String NOTIFICATION_SUBJECT_TEMPLATE = "atlas.templates.applications.email.subject";
    
    public EmailNotificationSender(JavaMailSender sender, TemplateRenderer renderer) {
    	this.sender = sender;
    	this.renderer = renderer;
    }
    
    @Autowired(required = true)
    public void setFrom(@Value("${notifications.email.from}") String from) {
        this.from = from;
    }

    @Autowired(required = true)
    public void setFromFriendlyName(@Value("${notifications.email.fromFriendlyName}") String fromFriendlyName) {
        this.fromFriendlyName = fromFriendlyName;
    }

    @Autowired(required = true)
    public void setTo(@Value("${notifications.email.to}") String to) {
        this.to = to;
    }
    
    public void sendNotificationOfPublisherRequest(Application app, Publisher publisher, String email, String reason) throws MessagingException, UnsupportedEncodingException {
    	 MimeMessage message = sender.createMimeMessage();
    	 
         MimeMessageHelper helper = new MimeMessageHelper(message, false, Charsets.UTF_8.name());
         SimpleModel model = new SimpleModel();
         model.put("publisher_key", publisher.key());
         model.put("publisher_title", publisher.title());
         model.put("email", email);
         model.put("reason", reason);
         model.put("slug", app.getSlug());
         model.put("application_title", app.getTitle());
         helper.setTo(this.to);     
         helper.setFrom(this.from, this.fromFriendlyName);
         helper.setText(renderer.render(NOTIFICATION_TEMPLATE, model), true);
         helper.setSubject(renderer.render(NOTIFICATION_SUBJECT_TEMPLATE, model));
         
         sender.send(message);
    }
}
