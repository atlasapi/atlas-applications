package org.atlasapi.application.query;


public class ApiKeyNotFoundException extends ApplicationFetchException {
    private static final long serialVersionUID = -8204400513571208163L;
    
    public ApiKeyNotFoundException(String apiKey) {
        super(apiKey);
    }
    
    @Override
    public String getMessage() {
        return "API key not found: " + this.getApiKey();
    }
}
