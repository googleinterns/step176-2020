package com.google.sps.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Arrays;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.collections4.MapIterator;

/*
 * Custom serializer for MultiKeyMap objects. This removes unnecessary text
 * present in the standard serialize that gets in the way of parsing on the client
 * and bloats transfer size.
*/
public class MultiKeyMapSerializer implements JsonSerializer<MultiKeyMap> {

  @Override
  public JsonElement serialize(
      MultiKeyMap map,
      Type typeOfT,
      JsonSerializationContext context) throws JsonParseException {
    JsonObject json = new JsonObject();
    JsonArray results = new JsonArray();

    MapIterator<MultiKey, Integer> it = map.mapIterator();
    while (it.hasNext()) {
      MultiKey key = it.next();
      Integer value = it.getValue();

      JsonObject result = new JsonObject();
      int i = 0;
      for (String keyString : (String[]) key.getKeys()) {
        result.addProperty("field" + i++, keyString);
      }
      result.addProperty("value", value);

      results.add(result);
    }

    json.add("results", results);
    return json;
  }
}
