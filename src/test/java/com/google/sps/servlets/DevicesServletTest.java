package com.google.sps.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.gson.Json;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/*
 * Test the status servlet.
 */
@RunWith(JUnit4.class)
public final class DevicesServletTest {

  private DevicesServlet servlet = new DevicesServlet();
  private HttpServletRequest request = mock(HttpServletRequest.class);
  private HttpServletResponse response = mock(HttpServletResponse.class);
  private final String LOGIN_URL = "/login";
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

  @Test
  public void userNotLoggedIn() throws IOException {
    User userStub = new User(TEST_USER_EMAIL, TEST_USER_AUTH_DOMAIN, TEST_USER_ID);
    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(false);
    when(mockedUserService.getCurrentUser()).thenReturn(userStub);

    servlet.setUserService(mockedUserService);
    servlet.doGet(request, response);

    verify(response).sendRedirect(LOGIN_URL);
    verify(mockedUserService, times(1)).isUserLoggedIn();
  }

  @Test
  public void userLoggedInDevicesSuccess() throws IOException {
    Util mockedUtil = mock(Util.class);
    User userFake = new User(TEST_USER_EMAIL, TEST_USER_AUTH_DOMAIN, TEST_USER_ID);
    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);
    when(mockedUtil.getAllDevices(TEST_USER_ID)).thenReturn(allDevices);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    servlet.setUserService(mockedUserService);
    servlet.setUtilObj(mockedUtil);
    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    String result = stringWriter.getBuffer().toString().trim();
    String expected = Json.toJson(allDevices);
    Assert.assertEquals(result, expected);
    
    verify(mockedUserService, times(1)).isUserLoggedIn();
    verify(mockedUserService, times(1)).getCurrentUser();
    verify(mockedUtil, times(1)).getAllDevices(TEST_USER_ID);
  }

  @Test
  public void userLoggedInDevicesFailure() throws IOException {
    Util mockedUtil = mock(Util.class);
    User userFake = new User(TEST_USER_EMAIL, TEST_USER_AUTH_DOMAIN, TEST_USER_ID);
    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);
    when(mockedUtil.getAllDevices(TEST_USER_ID)).thenThrow(IOException.class);

    servlet.setUserService(mockedUserService);
    servlet.setUtilObj(mockedUtil);
    servlet.doGet(request, response);
  
    verify(response).sendRedirect(AUTHORIZE_URL);  
    verify(mockedUserService, times(1)).isUserLoggedIn();
    verify(mockedUserService, times(1)).getCurrentUser();
    verify(mockedUtil, times(1)).getAllDevices(TEST_USER_ID);
  }

}
