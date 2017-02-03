package org.atlasapi.application.query;

import com.metabroadcast.applications.client.exceptions.ErrorCode;

public abstract class ApplicationFetchException extends Exception {
    private static final long serialVersionUID = 5835729721346196714L;
    private final String apiKey;
    private final ErrorCode errorCode;

    protected ApplicationFetchException(String apiKey, ErrorCode errorCode) {
        super();
        this.apiKey = apiKey;
        this.errorCode = errorCode;
    }
    
    public String getApiKey() {
        return apiKey;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
