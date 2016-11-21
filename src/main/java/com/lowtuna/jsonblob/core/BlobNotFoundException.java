package com.lowtuna.jsonblob.core;

import lombok.Getter;

@Getter
public class BlobNotFoundException extends Exception {
    private final String id;

    public BlobNotFoundException(String id) {
        this.id = id;
    }

    public BlobNotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }

    public BlobNotFoundException(String message, Throwable cause, String id) {
        super(message, cause);
        this.id = id;
    }

    public BlobNotFoundException(Throwable cause, String id) {
        super(cause);
        this.id = id;
    }

    public BlobNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String id) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.id = id;
    }
}
