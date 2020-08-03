package com.google.sps.data;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.commons.collections4.keyvalue.MultiKey;

/*
 * Class representing one row/entry in the aggregation response.  A NULL field value
 * means the field was not being aggregated by, and will be omitted from the JSON response
 */
public final class AggregationResponseEntry {

  private String assetId;
  private String location;
  private String user;
  private final int count;

  public AggregationResponseEntry(MultiKey key, int count, LinkedHashSet<AnnotatedField> fields) {

    String keys[] = (String[]) key.getKeys();
    int currKey = 0;

    Iterator<AnnotatedField> it = fields.iterator();
    while (it.hasNext()) {
      switch(it.next()) {
        case ASSET_ID:
          this.assetId = keys[currKey++];
          break;
        case LOCATION:
          this.location = keys[currKey++];
          break;
        case USER:
          this.user = keys[currKey++];
          break;
      }
    }

    this.count = count;
  }
}
