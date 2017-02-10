package com.lowtuna.jsonblob.core;

/**
 * Created by tburch on 11/15/16.
 */
public interface JsonBlobManager {
  String createBlob(String blob);

  String getBlob(String blobId) throws BlobNotFoundException;

  boolean updateBlob(String blobId, String blob) throws BlobNotFoundException;

  boolean deleteBlob(String blobId) throws BlobNotFoundException;
}
