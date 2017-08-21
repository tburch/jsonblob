package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by tburch on 2/9/17.
 */
@Slf4j
@RequiredArgsConstructor
public class UpdateBlobLastAccessedJob implements Runnable {
  private final Map<String, DateTime> updates;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final ObjectMapper om;

  @Override
  public void run() {
    log.info("Updating last access timestamp for {} blobs", updates.size());
    ListMultimap<File, Pair<String, DateTime>> updatesByMetadataFile = LinkedListMultimap.create();
    for (Map.Entry<String, DateTime> entry : updates.entrySet()) {
      File metadataFile = fileSystemJsonBlobManager.getMetaDataFile(entry.getKey());
      updatesByMetadataFile.put(metadataFile, Pair.of(entry.getKey(), entry.getValue()));
    }

    updatesByMetadataFile.keySet().stream().forEach(metadataFile -> {
      try {
        log.info(metadataFile.exists() ? "Reading metadata file at {}" : "No metadata file exists yet, so creating a new one", metadataFile.getAbsolutePath());
        BlobMetadataContainer metadataContainer = metadataFile.exists() ? om.readValue(fileSystemJsonBlobManager.readFile(metadataFile), BlobMetadataContainer.class) : new BlobMetadataContainer();
        updatesByMetadataFile.get(metadataFile).forEach(p -> metadataContainer.getLastAccessedByBlobId().put(p.getKey(), p.getValue()));

        log.info("Removing deleted blobs from last access map");
        Set<String> blobs = Sets.newHashSet(Lists.transform(Arrays.asList(metadataFile.getParentFile().listFiles()), f -> f.getName().split(".", 1)[0]));
        Set<String> deletedBlobs = metadataContainer.getLastAccessedByBlobId().keySet().parallelStream().filter(blobId -> blobs.contains(blobId)).collect(Collectors.toSet());
        deletedBlobs.stream().forEach(blobId -> metadataContainer.getLastAccessedByBlobId().remove(blobId));

        log.info("Writing metadata file at {}", metadataFile.getAbsolutePath());
        fileSystemJsonBlobManager.writeFile(metadataFile, om.writeValueAsString(metadataContainer));
      } catch (IOException e) {
        log.warn("Couldn't read/write metadata file at {}", metadataFile.getAbsolutePath(), e);
      }
    });

  }

}
