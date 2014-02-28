package com.lowtuna.jsonblob.core;

import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
public class BlobNotFoundException extends Exception {
    private final ObjectId id;

    public BlobNotFoundException(ObjectId id) {
        this.id = id;
    }

    public BlobNotFoundException(String message, ObjectId id) {
        super(message);
        this.id = id;
    }

    public BlobNotFoundException(String message, Throwable cause, ObjectId id) {
        super(message, cause);
        this.id = id;
    }

    public BlobNotFoundException(Throwable cause, ObjectId id) {
        super(cause);
        this.id = id;
    }

    public BlobNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ObjectId id) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.id = id;
    }
}
