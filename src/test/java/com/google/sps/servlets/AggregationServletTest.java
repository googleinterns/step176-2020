package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.sps.data.AnnotatedField;
import com.google.sps.data.ChromeOSDevice;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

  private final String LOGIN_URL = "/login";
  private final String HOME_URL = "/index.html";
  private final String AUTHORIZE_URL = "/authorize";
  private final String TEST_USER_ID = "testUserId";
  private final String TEST_USER_EMAIL = "testEmail";
  private final String TEST_USER_AUTH_DOMAIN = "testAuthDomain";

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
    MultiKeyMap<String, List<String>> expected = new MultiKeyMap<>();
    expected.put(new MultiKey(new String[] {ASSET_ID_ONE}),
        allDevices.stream().map(device -> device.getDeviceId()).collect(Collectors.toList()));

    MultiKeyMap<String, List<String>> actual = processData(allDevices, AnnotatedField.ASSET_ID);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void multipleResultEntries() {
    MultiKeyMap<String, List<String>> expected = new MultiKeyMap<>();
    expected.put(new MultiKey(
        new String[] {USER_ONE}), Arrays.asList(DEVICE_ONE.getDeviceId(), DEVICE_FOUR.getDeviceId()));
    expected.put(new MultiKey(
        new String[] {USER_TWO}), Arrays.asList(DEVICE_TWO.getDeviceId()));
    expected.put(new MultiKey(
        new String[] {USER_THREE}), Arrays.asList(DEVICE_THREE.getDeviceId(), DEVICE_FIVE.getDeviceId()));

    MultiKeyMap<String, List<String>> actual = processData(allDevices, AnnotatedField.USER);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void annotatedLocation() {
    MultiKeyMap<String, List<String>> expected = new MultiKeyMap<>();
    expected.put(new MultiKey(
        new String[] {LOCATION_ONE}),
        Arrays.asList(DEVICE_ONE.getDeviceId(), DEVICE_TWO.getDeviceId(), DEVICE_FIVE.getDeviceId()));
    expected.put(new MultiKey(
        new String[] {LOCATION_TWO}),
        Arrays.asList(DEVICE_THREE.getDeviceId(), DEVICE_FOUR.getDeviceId()));

    MultiKeyMap<String, List<String>> actual = processData(allDevices, AnnotatedField.LOCATION);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void multipleAggregationFields() {
    MultiKeyMap<String, List<String>> expected = new MultiKeyMap<>();
    expected.put(new MultiKey(
        new String[] {ASSET_ID_ONE, LOCATION_ONE}),
        Arrays.asList(DEVICE_ONE.getDeviceId(), DEVICE_TWO.getDeviceId(), DEVICE_FIVE.getDeviceId()));
    expected.put(new MultiKey(
        new String[] {ASSET_ID_ONE, LOCATION_TWO}),
        Arrays.asList(DEVICE_THREE.getDeviceId(), DEVICE_FOUR.getDeviceId()));

    MultiKeyMap<String, List<String>> actual = AggregationServlet.processData(allDevices,
        new LinkedHashSet<>(Arrays.asList(AnnotatedField.ASSET_ID, AnnotatedField.LOCATION)));

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
    Util mockedUtil = mock(Util.class);
    User userFake = new User(TEST_USER_EMAIL, TEST_USER_AUTH_DOMAIN, TEST_USER_ID);
    DatastoreService mockedDataObj = mock(DatastoreService.class);
    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);
    when(mockedUtil.getAllDevices(TEST_USER_ID)).thenReturn(allDevices);

    servlet.setUserService(mockedUserService);
    servlet.setUtilObj(mockedUtil);
    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  public void validMultiFieldArgumentReceivesSuccess() throws IOException{
    when(request.getParameter("aggregationField")).thenReturn("annotatedLocation,annotatedAssetId");
    setNewResponseWriter(response);
    Util mockedUtil = mock(Util.class);
    User userFake = new User(TEST_USER_EMAIL, TEST_USER_AUTH_DOMAIN, TEST_USER_ID);
    DatastoreService mockedDataObj = mock(DatastoreService.class);
    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);
    when(mockedUtil.getAllDevices(TEST_USER_ID)).thenReturn(allDevices);

    servlet.setUserService(mockedUserService);
    servlet.setUtilObj(mockedUtil);
    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  private void setNewResponseWriter(HttpServletResponse response) throws IOException{
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
  }

  /** Used for convenience in tests when only aggregating by one field*/
  private MultiKeyMap<String, List<String>> processData(List<ChromeOSDevice> devices, AnnotatedField field) {
    LinkedHashSet<AnnotatedField> fields = new LinkedHashSet<>();
    fields.add(field);

    return servlet.processData(devices, fields);
  }

}
