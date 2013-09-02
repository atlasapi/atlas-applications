package org.atlas.application.notification;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.atlasapi.application.Application;
import org.atlasapi.application.sources.UsageType;
import org.atlasapi.media.entity.Publisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.google.common.base.Charsets;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.webapp.soy.TemplateRenderer;

public class EmailNotificationSender {
	
	public static final Builder emailNotificationSender(JavaMailSender mailSender, TemplateRenderer renderer) {
		return new Builder(mailSender, renderer);
	}
	
	public final static class Builder {

		private final JavaMailSender sender;
		private final TemplateRenderer renderer;

		private String from;
		private String fromFriendlyName;
		private String to;
		    
		public Builder(JavaMailSender sender, TemplateRenderer renderer) {
			this.sender = sender;
			this.renderer = renderer;
		}
		
		public Builder withToField(String to) {
			this.to = to;	
			return this;
		}
		
		public Builder withFromField(String from) {
			this.from = from;
			return this;
		}
		public Builder withFriendlyFromName(String fromFriendlyName) {
			this.fromFriendlyName = fromFriendlyName;
			return this;
		}
		
		public EmailNotificationSender build() {
			EmailNotificationSender emailNotificationSender = new EmailNotificationSender(this.sender, this.renderer, this.from, this.fromFriendlyName, this.to);		
			return emailNotificationSender;
		}
	}
	
	private static final String NOTIFICATION_TEMPLATE = "atlas.templates.applications.email.body";
	private static final String NOTIFICATION_SUBJECT_TEMPLATE = "atlas.templates.applications.email.subject";
	
	private final JavaMailSender sender;
	private final TemplateRenderer renderer;

    private final String from;
    private final String fromFriendlyName;
    private final String to;

    private EmailNotificationSender(JavaMailSender sender, TemplateRenderer renderer, String from, String fromFriendlyName, String to) {
    	this.sender = sender;
    	this.renderer = renderer;
    	this.from = from;
    	this.fromFriendlyName = fromFriendlyName;
    	this.to = to;
    }
    
    public void sendNotificationOfPublisherRequest(Application app, Publisher publisher, UsageType usageType, String email, String reason, String appUrl) throws MessagingException, UnsupportedEncodingException {
    	 MimeMessage message = sender.createMimeMessage();
    	 
         MimeMessageHelper helper = new MimeMessageHelper(message, false, Charsets.UTF_8.name());
         SimpleModel model = new SimpleModel();
         model.put("publisher_key", publisher.key());
         model.put("publisher_title", publisher.title());
         model.put("usage_type", usageType.title());
         model.put("email", email);
         model.put("appUrl", appUrl);
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
