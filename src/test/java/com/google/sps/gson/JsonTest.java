package com.google.sps.gson;

import com.google.sps.data.ChromeOSDevice;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/*
 * Test the custom deserializer; assume that standard Gson functionality works properly.
 * Thus we only need to test deserializing the ChromeOSDevice and can assume the
 * ListDeviceResponse deserializes fine.
*/
@RunWith(JUnit4.class)
public final class JsonTest {

  @Test
  public void deserializeDevice() {
    String assetId = "ef249a";
    String location = "California";
    String user = "James";
    String deviceId = "93fc2a-c09afda81";
    String serialNumber = "SN11111";

    ChromeOSDevice expected =
        new ChromeOSDevice(assetId, location, user, deviceId, serialNumber);

    ChromeOSDevice actual = (ChromeOSDevice) Json.fromJson(
        "{\"annotatedAssetId\": \"" + assetId + "\", " +
        "\"annotatedLocation\": \"" + location + "\", " +
        "\"annotatedUser\": \"" + user + "\", " +
        "\"deviceId\": \"" + deviceId + "\", " +
        "\"serialNumber\": \"" + serialNumber + "\"}", ChromeOSDevice.class);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void deserializeDeviceMissingField(){
    String assetId = null;
    String location = "New Jersey";
    String user = null;
    String deviceId = "ae37f0-5bd7593ca";
    String serialNumber = "SN11111";

    ChromeOSDevice expected =
        new ChromeOSDevice(assetId, location, user, deviceId, serialNumber);

    ChromeOSDevice actual = (ChromeOSDevice) Json.fromJson(
        "{\"annotatedLocation\": \"" + location + "\", " +
        "\"deviceId\": \"" + deviceId + "\", " +
        "\"serialNumber\": \"" + serialNumber + "\"}", ChromeOSDevice.class);

    Assert.assertEquals(expected, actual);
  }

}
