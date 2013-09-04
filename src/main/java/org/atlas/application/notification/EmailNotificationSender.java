package org.atlas.application.notification;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.atlasapi.application.Application;
import org.atlasapi.application.sources.SourceRequest;
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
		private String adminTo;
		    
		public Builder(JavaMailSender sender, TemplateRenderer renderer) {
			this.sender = sender;
			this.renderer = renderer;
		}
		
		public Builder withAdminToField(String to) {
			this.adminTo = to;	
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
			EmailNotificationSender emailNotificationSender = new EmailNotificationSender(this.sender, this.renderer, this.from, this.fromFriendlyName, this.adminTo);		
			return emailNotificationSender;
		}
	}
	
	private static final String ADMIN_NOTIFICATION_TEMPLATE = "atlas.templates.applications.admin.email.body";
	private static final String ADMIN_NOTIFICATION_SUBJECT_TEMPLATE = "atlas.templates.applications.admin.email.subject";
    private static final String USER_NOTIFICATION_TEMPLATE = "atlas.templates.applications.user.email.body";
    private static final String USER_NOTIFICATION_SUBJECT_TEMPLATE = "atlas.templates.applications.user.email.subject";
    private static final String USER_SUCCESS_NOTIFICATION_TEMPLATE = "atlas.templates.applications.user.success.email.body";
    private static final String USER_SUCCESS_NOTIFICATION_SUBJECT_TEMPLATE = "atlas.templates.applications.user.success.email.subject";
    
	private final JavaMailSender sender;
	private final TemplateRenderer renderer;

    private final String from;
    private final String fromFriendlyName;
    private final String adminTo;

    private EmailNotificationSender(JavaMailSender sender, TemplateRenderer renderer, String from, String fromFriendlyName, String adminTo) {
    	this.sender = sender;
    	this.renderer = renderer;
    	this.from = from;
    	this.fromFriendlyName = fromFriendlyName;
    	this.adminTo = adminTo;
    }
    
    public void sendNotificationOfPublisherRequestToAdmin(Application app, SourceRequest sourceRequest) throws MessagingException, UnsupportedEncodingException {
    	 MimeMessage message = sender.createMimeMessage();
    	 
         MimeMessageHelper helper = new MimeMessageHelper(message, false, Charsets.UTF_8.name());
         SimpleModel model = new SimpleModel();
         model.put("publisher_key", sourceRequest.getPublisher().key());
         model.put("publisher_title", sourceRequest.getPublisher().title());
         model.put("usage_type", sourceRequest.getUsageType().title());
         model.put("email", sourceRequest.getEmail());
         model.put("appUrl", sourceRequest.getAppUrl());
         model.put("reason", sourceRequest.getReason());
         model.put("slug", app.getSlug());
         model.put("application_title", app.getTitle());
         helper.setTo(this.adminTo);     
         helper.setFrom(this.from, this.fromFriendlyName);
         helper.setText(renderer.render(ADMIN_NOTIFICATION_TEMPLATE, model), true);
         helper.setSubject(renderer.render(ADMIN_NOTIFICATION_SUBJECT_TEMPLATE, model));
         
         sender.send(message);
    }
    
    public void sendNotificationOfPublisherRequestToUser(Application app, SourceRequest sourceRequest) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = sender.createMimeMessage();
        
        MimeMessageHelper helper = new MimeMessageHelper(message, false, Charsets.UTF_8.name());
        SimpleModel model = new SimpleModel();
        model.put("publisher_key", sourceRequest.getPublisher().key());
        model.put("publisher_title", sourceRequest.getPublisher().title());
        model.put("usage_type", sourceRequest.getUsageType().title());
        model.put("appUrl", sourceRequest.getAppUrl());
        model.put("reason", sourceRequest.getReason());
        model.put("slug", app.getSlug());
        model.put("application_title", app.getTitle());
        helper.setTo(sourceRequest.getEmail());     
        helper.setFrom(this.from, this.fromFriendlyName);
        helper.setText(renderer.render(USER_NOTIFICATION_TEMPLATE, model), true);
        helper.setSubject(renderer.render(USER_NOTIFICATION_SUBJECT_TEMPLATE, model));
        
        sender.send(message);
   }
    
    public void sendNotificationOfPublisherRequestSuccessToUser(Application app, SourceRequest sourceRequest) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = sender.createMimeMessage();
        
        MimeMessageHelper helper = new MimeMessageHelper(message, false, Charsets.UTF_8.name());
        SimpleModel model = new SimpleModel();
        model.put("publisher_key", sourceRequest.getPublisher().key());
        model.put("publisher_title", sourceRequest.getPublisher().title());
        model.put("usage_type", sourceRequest.getUsageType().title());
        model.put("appUrl", sourceRequest.getAppUrl());
        model.put("reason", sourceRequest.getReason());
        model.put("slug", app.getSlug());
        model.put("application_title", app.getTitle());
        helper.setTo(sourceRequest.getEmail());     
        helper.setFrom(this.from, this.fromFriendlyName);
        helper.setText(renderer.render(USER_SUCCESS_NOTIFICATION_TEMPLATE, model), true);
        helper.setSubject(renderer.render(USER_SUCCESS_NOTIFICATION_SUBJECT_TEMPLATE, model));
        
        sender.send(message);
   }
}
