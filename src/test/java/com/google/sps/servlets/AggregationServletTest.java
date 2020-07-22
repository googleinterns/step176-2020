package com.google.sps.servlets;

import com.google.sps.data.ChromeOSDevice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/*
 * Test the aggregation functionality to ensure it reports accurate counts and
 * throws errors when appropriate.  Eventually will need to add tests for
 * aggregating by multiple fields at once.
 */
@RunWith(JUnit4.class)
public final class AggregationServletTest {

  private final String LOCATION_ONE = "New Jersey";
  private final String LOCATION_TWO = "California";

  private final String USER_ONE = "James";
  private final String USER_TWO = "Josiah";
  private final String USER_THREE = "Jeremy";

  private final String ASSET_ID_ONE = "12345";

  private final ChromeOSDevice DEVICE_ONE =
      new ChromeOSDevice(ASSET_ID_ONE, LOCATION_ONE, USER_ONE, "deviceId", "serialNumber");
  private final ChromeOSDevice DEVICE_TWO =
      new ChromeOSDevice(ASSET_ID_ONE, LOCATION_ONE, USER_TWO, "deviceId", "serialNumber");
  private final ChromeOSDevice DEVICE_THREE =
      new ChromeOSDevice(ASSET_ID_ONE, LOCATION_TWO, USER_THREE, "deviceId", "serialNumber");
  private final ChromeOSDevice DEVICE_FOUR =
      new ChromeOSDevice(ASSET_ID_ONE, LOCATION_TWO, USER_ONE, "deviceId", "serialNumber");
  private final ChromeOSDevice DEVICE_FIVE =
      new ChromeOSDevice(ASSET_ID_ONE, LOCATION_ONE, USER_THREE, "deviceId", "serialNumber");

  private final List<ChromeOSDevice> allDevices = new ArrayList<>(
      Arrays.asList(DEVICE_ONE, DEVICE_TWO, DEVICE_THREE, DEVICE_FOUR, DEVICE_FIVE));

  @Test
  public void onlyOneUniqueField() {
    AggregationServlet servlet = new AggregationServlet();

    Map<String, Integer> expected = new HashMap<>();
    expected.put(ASSET_ID_ONE, 5);

    Map<String, Integer> actual = servlet.processData(allDevices, "annotatedAssetId");

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void manyFields() {
    AggregationServlet servlet = new AggregationServlet();

    Map<String, Integer> expected = new HashMap<>();
    expected.put(USER_ONE, 2);
    expected.put(USER_TWO, 1);
    expected.put(USER_THREE, 2);

    Map<String, Integer> actual = servlet.processData(allDevices, "annotatedUser");

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void annotatedLocation() {
    AggregationServlet servlet = new AggregationServlet();

    Map<String, Integer> expected = new HashMap<>();
    expected.put(LOCATION_ONE, 3);
    expected.put(LOCATION_TWO, 2);

    Map<String, Integer> actual = servlet.processData(allDevices, "annotatedLocation");

    Assert.assertEquals(expected, actual);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidArgument() {
    AggregationServlet servlet = new AggregationServlet();

    Map<String, Integer> actual = servlet.processData(allDevices, "deviceId");
  }

}
