package com.lowtuna.jsonblob.core;

import com.google.common.collect.Maps;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by tburch on 2/9/17.
 */
@Data
public class BlobMetadataContainer {
  private Map<String, DateTime> lastAccessedByBlobId = Maps.newHashMap();
}
