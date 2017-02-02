package org.atlasapi.application.query;


public class InvalidIpForApiKeyException extends ApplicationFetchException {
    private static final long serialVersionUID = -8204400513571208163L;
    
    public InvalidIpForApiKeyException(String apiKey) {
        super(apiKey);
    }

    @Override
    public String getMessage() {
        return "Invalid IP address for API key: " + this.getApiKey();
    }
}
