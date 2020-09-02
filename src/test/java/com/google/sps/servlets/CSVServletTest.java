package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.sps.data.AnnotatedField;
import com.google.sps.data.ChromeOSDevice;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import javax.servlet.ServletOutputStream;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/*
 * Test the aggregation functionality to ensure it reports accurate counts and
 * throws errors when appropriate.  Eventually will need to add tests for
 * aggregating by multiple fields at once.
 */
@RunWith(JUnit4.class)
public final class CSVServletTest {

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

  private UserService mockedUserService;
  private Util mockedUtil;
  private User userFake;

  @Before
  public void setUp() {
    mockedUserService = mock(UserService.class);
    mockedUtil = mock(Util.class);
    userFake = new User(TEST_USER_EMAIL, TEST_USER_AUTH_DOMAIN, TEST_USER_ID);

    servlet.setUserService(mockedUserService);
    servlet.setUtilObj(mockedUtil);
  }

  @Test
  public void userNotLoggedIn() throws IOException {
    when(mockedUserService.isUserLoggedIn()).thenReturn(false);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);

    servlet.doGet(request, response);

    verify(response).sendRedirect(servlet.LOGIN_URL);
    verify(mockedUserService, times(1)).isUserLoggedIn();
  }

  @Test
  public void exceptionWhileGettingDevices() {

  }

  @Test
  public void runsToSuccess() {

  }

}
