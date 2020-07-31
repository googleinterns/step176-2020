package com.google.sps.servlets;

import com.google.sps.data.AnnotatedField;
import com.google.sps.data.ChromeOSDevice;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  private AggregationServlet servlet = new AggregationServlet();
  private HttpServletRequest request = mock(HttpServletRequest.class);
  private HttpServletResponse response = mock(HttpServletResponse.class);

  @Test
  public void onlyOneUniqueField() {
    MultiKeyMap<String, Integer> expected = new MultiKeyMap<>();
    expected.put(new MultiKey(new String[] {ASSET_ID_ONE}), 5);

    MultiKeyMap<String, Integer> actual = processData(allDevices, AnnotatedField.ASSET_ID);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void multipleResultEntries() {
    MultiKeyMap<String, Integer> expected = new MultiKeyMap<>();
    expected.put(new MultiKey(new String[] {USER_ONE}), 2);
    expected.put(new MultiKey(new String[] {USER_TWO}), 1);
    expected.put(new MultiKey(new String[] {USER_THREE}), 2);

    MultiKeyMap<String, Integer> actual = processData(allDevices, AnnotatedField.USER);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void annotatedLocation() {
    MultiKeyMap<String, Integer> expected = new MultiKeyMap<>();
    expected.put(new MultiKey(new String[] {LOCATION_ONE}), 3);
    expected.put(new MultiKey(new String[] {LOCATION_TWO}), 2);

    MultiKeyMap<String, Integer> actual = processData(allDevices, AnnotatedField.LOCATION);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void multipleAggregationFields() {
    MultiKeyMap<String, Integer> expected = new MultiKeyMap<>();
    expected.put(new MultiKey(new String[] {ASSET_ID_ONE, LOCATION_ONE}), 3);
    expected.put(new MultiKey(new String[] {ASSET_ID_ONE, LOCATION_TWO}), 2);

    MultiKeyMap<String, Integer> actual = AggregationServlet.processData(allDevices,
        new HashSet<>(Arrays.asList(AnnotatedField.LOCATION, AnnotatedField.ASSET_ID)));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void invalidMultiFieldArgumentReceivesBadRequest() throws IOException {
    when(request.getParameter("aggregationField")).thenReturn("annotatedLocation,deviceId");
    setNewResponseWriter(response);

    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void invalidArgumentReceivesBadRequest() throws IOException {
    when(request.getParameter("aggregationField")).thenReturn("deviceId");
    setNewResponseWriter(response);

    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void nullArgumentReceivesBadRequest() throws IOException {
    when(request.getParameter("aggregationField")).thenReturn(null);
    setNewResponseWriter(response);

    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  @Test
  public void validArgumentReceivesSuccess() throws IOException{
    when(request.getParameter("aggregationField")).thenReturn("annotatedLocation");
    setNewResponseWriter(response);

    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  public void validMultiFieldArgumentReceivesSuccess() throws IOException{
    when(request.getParameter("aggregationField")).thenReturn("annotatedLocation,annotatedAssetId");
    setNewResponseWriter(response);

    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  private void setNewResponseWriter(HttpServletResponse response) throws IOException{
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
  }

  /** Used for convenience in tests when only aggregating by one field*/
  private MultiKeyMap<String, Integer> processData(List<ChromeOSDevice> devices, AnnotatedField field) {
    Set<AnnotatedField> fields = new HashSet<>();
    fields.add(field);

    return servlet.processData(devices, fields);
  }
}
