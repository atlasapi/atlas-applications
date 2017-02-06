package org.atlasapi.application.query;

import com.metabroadcast.applications.client.exceptions.ErrorCode;

public class InvalidApiKeyException extends ApplicationFetchException {
    private static final long serialVersionUID = -8204400513571208163L;
    
    private InvalidApiKeyException(String apiKey, ErrorCode errorCode) {
        super(apiKey, errorCode);
    }

    public static InvalidApiKeyException create(String apiKey, ErrorCode errorCode) {
        return new InvalidApiKeyException(apiKey, errorCode);
    }
    @Override
    public String getMessage() {
        return String.format(
                "Invalid api key: %s",
                this.getApiKey()
        );
    }
}
