package com.google.sps.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.commons.collections4.keyvalue.MultiKey;

/*
 * Class representing one row/entry in the aggregation response.  A NULL field value
 * means the field was not being aggregated by, and will be omitted from the JSON response
 */
public final class AggregationResponseEntry {

  private String annotatedAssetId;
  private String annotatedLocation;
  private String annotatedUser;
  private final int count;
  private List<String> deviceIds;
  private List<String> serialNumbers;

  public AggregationResponseEntry(
      MultiKey key,
      List<ChromeOSDevice> devices,
      LinkedHashSet<AnnotatedField> fields) {

    String keys[] = (String[]) key.getKeys();
    int currKey = 0;

    Iterator<AnnotatedField> it = fields.iterator();
    while (it.hasNext()) {
      switch(it.next()) {
        case ASSET_ID:
          this.annotatedAssetId = keys[currKey++];
          break;
        case LOCATION:
          this.annotatedLocation = keys[currKey++];
          break;
        case USER:
          this.annotatedUser = keys[currKey++];
          break;
      }
    }

    this.count = devices.size();

    this.deviceIds = new ArrayList<String>();
    this.serialNumbers = new ArrayList<String>();
    for (ChromeOSDevice device : devices) {
      this.deviceIds.add(device.getDeviceId());
      this.serialNumbers.add(device.getSerialNumber());
    }
  }
}
