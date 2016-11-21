package com.lowtuna.jsonblob.core;

import com.fasterxml.uuid.Generators;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.Charsets;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by tburch on 11/15/16.
 */
@RequiredArgsConstructor
public class FileSystemJsonBlobManager implements JsonBlobManager {
  private static final DateTimeFormatter DIRECTORY_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd");

  private final File blobDataDirectory;

  private File getBlobFile(String blobId, DateTime createdTimestamp) {
    File subDir = new File(blobDataDirectory, DIRECTORY_FORMAT.print(createdTimestamp));
    return new File(subDir, blobId + ".json.gz");
  }

  private void writeFile(File file, String content) throws IOException {
    try(Writer writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), Charsets.UTF_8)) {
        writer.write(content);
    }
  }

  private String readFile(File file) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      sb.append(line);
    }
    return sb.toString();
  }

  public Optional<DateTime> resolveTimestamp(String blobId) {
    try {
      UUID uuid = UUID.fromString(blobId);

      Calendar uuidEpoch = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      uuidEpoch.clear();
      uuidEpoch.set(1582, 9, 15, 0, 0, 0); // 9 = October
      long epochMillis = uuidEpoch.getTime().getTime();

      long time = (uuid.timestamp() / 10000L) + epochMillis;

      return Optional.of(new DateTime(time));
    } catch (IllegalArgumentException e) {
      try {
        ObjectId objectId = new ObjectId(blobId);
        return Optional.of(new DateTime(objectId.getTime()));
      } catch (IllegalArgumentException e1) {
        return Optional.absent();
      }
    }
  }

  @Override
  public String createBlob(String blob) throws IllegalArgumentException {
    if (!MongoDbJsonBlobManager.isValidJson(blob)) {
      throw new IllegalArgumentException();
    }

    UUID uuid = Generators.timeBasedGenerator().generate();

    return createBlob(blob, uuid.toString());
  }

  public String createBlob(String blob, String blobId) {
    Optional<DateTime> createTimestamp = resolveTimestamp(blobId);
    if (!createTimestamp.isPresent()) {
      throw new IllegalStateException("Couldn't generate create timestamp from " + blobId);
    }

    File blobFile = getBlobFile(blobId, createTimestamp.get());
    blobFile.getParentFile().mkdirs();
    try {
      writeFile(blobFile, blob);
      return blobId;
    } catch (IOException e) {
      throw new IllegalStateException("Couldn't write blob", e);
    }
  }

  @Override
  public String getBlob(String blobId) throws BlobNotFoundException {
    Optional<DateTime> createTimestamp = resolveTimestamp(blobId);
    if (!createTimestamp.isPresent()) {
      throw new BlobNotFoundException(blobId);
    }

    File blobFile = getBlobFile(blobId, createTimestamp.get());

    try {
      return readFile(blobFile);
    } catch (FileNotFoundException e) {
      throw new BlobNotFoundException(blobId);
    } catch (IOException e) {
      throw new RuntimeException("Couldn't read blob", e);
    }
  }

  @Override
  public boolean updateBlob(String blobId, String blob) throws IllegalArgumentException, BlobNotFoundException {
    return updateBlob(blobId, blob, false);
  }

  public boolean updateBlob(String blobId, String blob, boolean forceWrite) throws IllegalArgumentException, BlobNotFoundException {
    Optional<DateTime> createTimestamp = resolveTimestamp(blobId);
    if (!createTimestamp.isPresent()) {
      throw new BlobNotFoundException(blobId);
    }

    File blobFile = getBlobFile(blobId, createTimestamp.get());
    if (!blobFile.exists() && !forceWrite) {
      throw new BlobNotFoundException(blobId);
    }

    try {
      writeFile(blobFile, blob);
      return true;
    } catch (IOException e) {
      throw new IllegalStateException("Couldn't write blob", e);
    }
  }

  @Override
  public boolean deleteBlob(String blobId) throws BlobNotFoundException {
    Optional<DateTime> createTimestamp = resolveTimestamp(blobId);
    if (!createTimestamp.isPresent()) {
      throw new BlobNotFoundException(blobId);
    }

    File blobFile = getBlobFile(blobId, createTimestamp.get());
    if (!blobFile.exists()) {
      throw new BlobNotFoundException(blobId);
    }

    return blobFile.delete();
  }

  @Override
  public boolean isDeleteEnabled() {
    return true;
  }

  public boolean blobExists(String blobId) {
    Optional<DateTime> createTimestamp = resolveTimestamp(blobId);
    if (!createTimestamp.isPresent()) {
      return false;
    }

    File blobFile = getBlobFile(blobId, createTimestamp.get());
    return blobFile.exists();
  }

}
