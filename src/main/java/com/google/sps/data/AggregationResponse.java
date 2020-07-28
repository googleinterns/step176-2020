package com.google.sps.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.collections4.MapIterator;

/*
 * Class representing successful response to Aggregation Servlet request.
 * Contains the data for the device counts according to the specified aggregation fields
*/
public final class AggregationResponse {

  private final List<AggregationResponseEntry> response;

  public AggregationResponse(MultiKeyMap map, Set<AnnotatedField> fields) {
    response = new ArrayList<>();

    MapIterator<MultiKey, Integer> it = map.mapIterator();
    while (it.hasNext()) {
      response.add(new AggregationResponseEntry(it.next(), it.getValue(), fields));
    }

  }
}
