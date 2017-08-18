package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Striped;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.concurrent.GuardedBy;
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
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by tburch on 11/15/16.
 */
@Slf4j
public class FileSystemJsonBlobManager implements JsonBlobManager, Runnable, Managed {
  private static final DateTimeFormatter DIRECTORY_FORMAT = DateTimeFormat.forPattern("yyyy/MM/dd");

  @GuardedBy("lastAccessedLock")
  private ConcurrentMap<String, DateTime> lastAccessedUpdates = Maps.newConcurrentMap();
  private ReentrantReadWriteLock lastAccessedLock = new ReentrantReadWriteLock();
  private final Striped<ReadWriteLock> blobStripedLocks = Striped.lazyWeakReadWriteLock(5000);

  private final File blobDataDirectory;
  private final ScheduledExecutorService scheduledExecutorService;
  private final ScheduledExecutorService cleanupScheduledExecutorService;
  private final ObjectMapper objectMapper;
  private final Duration blobAccessTtl;
  @Getter
  private final boolean deleteEnabled;

  public FileSystemJsonBlobManager(File blobDataDirectory, ScheduledExecutorService scheduledExecutorService, ScheduledExecutorService cleanupScheduledExecutorService, ObjectMapper objectMapper, Duration blobAccessTtl, boolean deleteEnabled) {
    this.blobDataDirectory = blobDataDirectory;
    this.scheduledExecutorService = scheduledExecutorService;
    this.cleanupScheduledExecutorService = cleanupScheduledExecutorService;
    this.objectMapper = objectMapper;
    this.blobAccessTtl = blobAccessTtl;
    this.deleteEnabled = deleteEnabled;

    blobDataDirectory.mkdirs();
  }

  private File getBlobDirectory(DateTime createdTimestamp) {
    return new File(blobDataDirectory, DIRECTORY_FORMAT.print(createdTimestamp));
  }

  private File getBlobFile(String blobId, DateTime createdTimestamp) {
    File subDir = getBlobDirectory(createdTimestamp);
    return new File(subDir, blobId + ".json.gz");
  }

  File getMetaDataFile(String blobId) {
    Optional<DateTime> createTimestamp = resolveTimestamp(blobId);
    if (!createTimestamp.isPresent()) {
      throw new IllegalStateException("Couldn't generate create timestamp from " + blobId);
    }

    File blobDirectory = getBlobDirectory(createTimestamp.get());
    return getMetaDataFile(blobDirectory);
  }

  File getMetaDataFile(File blobDirectory) {
    return new File(blobDirectory, "blobMetadata" + ".json.gz");
  }

  Optional<DateTime> resolveTimestamp(String blobId) {
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
    if (!isValidJson(blob)) {
      throw new IllegalArgumentException();
    }

    UUID uuid = Generators.timeBasedGenerator().generate();
    String blobId = createBlob(blob, uuid.toString());

    return blobId;
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
      String content = readFile(blobFile);

      updateLastAccessedTimestamp(blobId, createTimestamp.get());

      return content;
    } catch (FileNotFoundException e) {
      throw new BlobNotFoundException(blobId);
    } catch (IOException e) {
      throw new RuntimeException("Couldn't read blob", e);
    }
  }

  @Override
  public boolean updateBlob(String blobId, String blob) throws IllegalArgumentException, BlobNotFoundException {
    Optional<DateTime> createTimestamp = resolveTimestamp(blobId);
    if (!createTimestamp.isPresent()) {
      throw new BlobNotFoundException(blobId);
    }

    File blobFile = getBlobFile(blobId, createTimestamp.get());
    if (!blobFile.exists()) {
      throw new BlobNotFoundException(blobId);
    }

    try {
      writeFile(blobFile, blob);

      updateLastAccessedTimestamp(blobId, createTimestamp.get());

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

    boolean deleted = deleteFile(blobFile);

    updateLastAccessedTimestamp(blobId, createTimestamp.get());

    return deleted;
  }

  private void updateLastAccessedTimestamp(String blobId, DateTime createTimestamp) {
    DateTime now = DateTime.now();
    if (now.toLocalDate().equals(createTimestamp.toLocalDate())) {
      return;
    }

    Lock lock = lastAccessedLock.writeLock();
    try {
      lock.lock();
      lastAccessedUpdates.put(blobId, now);
    } finally {
      lock.unlock();
    }
  }

  private boolean isValidJson(String json) {
    try {
      JSON.parse(json);
      return true;
    } catch (JSONParseException e) {
      return false;
    }
  }

  void writeFile(File file, String content) throws IOException {
    Lock lock = blobStripedLocks.get(file.getAbsolutePath()).writeLock();
    try {
      lock.lock();
      try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), Charsets.UTF_8)) {
        writer.write(content);
      }
    } finally {
      lock.unlock();
    }
  }

  String readFile(File file) throws IOException {
    Lock lock = blobStripedLocks.get(file.getAbsolutePath()).readLock();
    try {
      lock.lock();
      StringBuilder sb = new StringBuilder();
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    } finally {
      lock.unlock();
    }
  }

  boolean deleteFile(File file) {
    Lock lock = blobStripedLocks.get(file.getAbsolutePath()).writeLock();
    try {
      lock.lock();
      return file.delete();
    } finally {
      lock.unlock();
    }
  }

  public boolean blobExists(String blobId) {
    Optional<DateTime> createTimestamp = resolveTimestamp(blobId);
    if (!createTimestamp.isPresent()) {
      return false;
    }

    File blobFile = getBlobFile(blobId, createTimestamp.get());
    return blobFile.exists();
  }

  @Override
  public void run() {
    Map<String, DateTime> lastAccessedUpdates = Maps.newHashMap();
    Lock lock = lastAccessedLock.writeLock();
    try {
      lock.lock();
      lastAccessedUpdates.putAll(this.lastAccessedUpdates);
      this.lastAccessedUpdates.clear();
    } finally {
      lock.unlock();
    }

    if (lastAccessedUpdates.isEmpty()) {
      return;
    }

    log.debug("Updating last accessed time for {} blobs", lastAccessedUpdates.size());
    scheduledExecutorService.submit(new UpdateBlobLastAccessedJob(lastAccessedUpdates, this, objectMapper));
  }

  @Override
  public void start() throws Exception {
    log.info("Scheduling the updating of blob last accessed timestamps");
    scheduledExecutorService.scheduleWithFixedDelay(this, 1, 1, TimeUnit.MINUTES);

    log.info("Scheduling blob cleanup job");
    BlobDataDirectoryCleaner dataDirectoryCleaner = new BlobDataDirectoryCleaner(blobDataDirectory.toPath(), blobAccessTtl, this,objectMapper);

    cleanupScheduledExecutorService.scheduleWithFixedDelay(dataDirectoryCleaner, 0, 1, TimeUnit.DAYS);
  }

  @Override
  public void stop() throws Exception {
    //nothing to do
  }
}
