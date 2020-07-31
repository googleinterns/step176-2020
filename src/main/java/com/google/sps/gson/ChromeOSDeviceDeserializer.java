package com.google.sps.gson;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.sps.data.ChromeOSDevice;
import java.lang.reflect.Type;

/*
 * Custom deserializer for ChromeOSDevice objects. This is necessary because the default Gson
 * deserializer does not use the constructor, so there is no way to sanitize the input.
 */
public final class ChromeOSDeviceDeserializer implements JsonDeserializer<ChromeOSDevice> {

  private static final Gson GSON = new Gson();

  @Override
  public ChromeOSDevice deserialize(
      JsonElement json,
      Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    final ChromeOSDevice device = GSON.fromJson(json, ChromeOSDevice.class);
    device.sanitize();
    return device;
  }
}
