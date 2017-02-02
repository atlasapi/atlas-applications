package org.atlasapi.application.query;


public abstract class ApplicationFetchException extends Exception {
    private static final long serialVersionUID = 5835729721346196714L;
    private final String apiKey;

    public ApplicationFetchException(String apiKey) {
        super();
        this.apiKey = apiKey;
    }
    
    public String getApiKey() {
        return apiKey;
    }
}
