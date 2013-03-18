package org.atlasapi.application.query;

public class InvalidAPIKeyException extends Exception {
	private String apiKey;

	public InvalidAPIKeyException() {
		super();
	}

	public InvalidAPIKeyException(String message, String apiKey) {
		super(message);
		this.apiKey = apiKey;
	}
	
	public String getAPIKey() {
		return apiKey;
	}
}
