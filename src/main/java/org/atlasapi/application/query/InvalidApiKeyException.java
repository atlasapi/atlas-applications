package org.atlasapi.application.query;

import com.metabroadcast.applications.client.exceptions.ErrorCode;

public class InvalidApiKeyException extends ApplicationFetchException {
    private static final long serialVersionUID = -8204400513571208163L;
    
    public InvalidApiKeyException(String apiKey, ErrorCode errorCode) {
        super(apiKey, errorCode);
    }

    @Override
    public String getMessage() {
        return "Error fetching application for apikey: " + this.getApiKey() + " - " + this.getErrorCode();
    }
}
