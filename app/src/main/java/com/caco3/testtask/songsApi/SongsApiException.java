package com.caco3.testtask.songsApi;

import org.json.JSONException;

import java.io.IOException;

/**
 * Represents an exception which may be thrown by {@link SongsApi}.
 */
public class SongsApiException extends Exception {

    public SongsApiException(String message) {
        super(message);
    }

    public SongsApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public SongsApiException(Throwable cause) {
        super(cause);
    }

    /**
     * Tests whether the current exception was caused by network issue
     * @return true if exception caused by network issue, false otherwise
     */
    public boolean isCausedByNetworkIssue(){
        return getCause() instanceof IOException;
    }

    /**
     * Tests whether the current exception was caused by json parse errors
     * @return true if exception caused by by json parse errors, false otherwise
     */
    public boolean isCausedByJsonParseErrors(){
        return getCause() instanceof JSONException;
    }
}
