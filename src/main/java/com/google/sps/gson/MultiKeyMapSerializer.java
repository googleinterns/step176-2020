package com.google.sps.gson;

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

    MapIterator<MultiKey, Integer> it = map.mapIterator();
    while (it.hasNext()) {
      MultiKey key = it.next();
      Integer value = it.getValue();

      String keyString = Arrays.toString(key.getKeys());

      // Remove the brackets[] at the beginning and end of the key
      json.addProperty(keyString.substring(1, keyString.length() - 1), value);
    }

    return json;
  }
}
