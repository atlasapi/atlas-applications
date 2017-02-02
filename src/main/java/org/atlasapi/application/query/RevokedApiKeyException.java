package org.atlasapi.application.query;


public class RevokedApiKeyException extends ApplicationFetchException {
    private static final long serialVersionUID = -8204400513571208163L;
    
    public RevokedApiKeyException(String apiKey) {
        super(apiKey);
    }

    @Override
    public String getMessage() {
        return "Revoked API key: " + this.getApiKey();
    }
}
