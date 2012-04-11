package org.atlas.application.notification;

import java.util.Properties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class JavaMailSenderFactory implements FactoryBean<JavaMailSender> {
	private final JavaMailSenderImpl sender;

	public JavaMailSenderFactory() {
		sender = new JavaMailSenderImpl();
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.localhost", "localhost");
		sender.setJavaMailProperties(properties);
	}

	@Override
	public JavaMailSender getObject() throws Exception {
		return sender;
	}

	@Override
	public Class<?> getObjectType() {
		return JavaMailSender.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setHost(@Value("${notifications.email.host}") String host) {
		sender.setHost(host);
		System.out.print("******** HOST:");
		System.out.println(sender.getHost());
				
	}

	public void setUsername(@Value("${notifications.email.username}") String username) {
		sender.setUsername(username);
	}

	public void setPassword(@Value("${notifications.email.password}") String password) {
		sender.setPassword(password);
	}
}
