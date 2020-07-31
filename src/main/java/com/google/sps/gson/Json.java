package com.google.sps.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.sps.gson.ChromeOSDeviceDeserializer;
import com.google.sps.data.ChromeOSDevice;
import org.apache.commons.collections4.map.MultiKeyMap;

/*
 * Class providing the ability to serialize and deserialize Json using Gson and custom-defined
 * deserializers where appropriate. Should be used by all classes not in this directory when
 * needing to serialize/deserialize something.
 */
public class Json {

  private static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(ChromeOSDevice.class, new ChromeOSDeviceDeserializer())
      .create();


  public static Object fromJson(String json, Class cls) {
    return GSON.fromJson(json, cls);
  }

  public static String toJson(Object obj) {
    return GSON.toJson(obj);
  }
}
