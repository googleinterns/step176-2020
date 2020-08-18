package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/*
 * Test the authorize servlet.
 */
@RunWith(JUnit4.class)
public final class AuthorizeServletTest {

  private AuthorizeServlet servlet = new AuthorizeServlet();
  private HttpServletRequest request = mock(HttpServletRequest.class);
  private HttpServletResponse response = mock(HttpServletResponse.class);
  private final String LOGIN_URL = "/login";
  private final String HOME_URL = "/index.html";
  private final String AUTHORIZE_URL = "/authorize";
  private final String TEST_USER_ID = "testUserId";
  private final String TEST_USER_EMAIL = "testEmail";
  private final String TEST_USER_AUTH_DOMAIN = "testAuthDomain";
  private final String TEST_AUTH_CODE = "authCode";
  private final String TEST_REFRESH_TOKEN = "refreshToken";
  private final String REQUEST_PARAM_KEY_CODE = "code";

  private UserService mockedUserService;
  private Util mockedUtil;
  private User userFake;
  private DatastoreService mockedDataObj;

  @Before
  public void setUp() {
    mockedUserService = mock(UserService.class);
    mockedUtil = mock(Util.class);
    userFake = new User(TEST_USER_EMAIL, TEST_USER_AUTH_DOMAIN, TEST_USER_ID);
    mockedDataObj = mock(DatastoreService.class);
    mockedUserService = mock(UserService.class);
  }

  @Test
  public void userNotLoggedIn() throws IOException {
    when(mockedUserService.isUserLoggedIn()).thenReturn(false);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);

    servlet.setUserService(mockedUserService);
    servlet.doPost(request, response);

    verify(response).sendRedirect(LOGIN_URL);
    verify(mockedUserService, times(1)).isUserLoggedIn();
  }

  @Test
  public void userLoggedInSuccess() throws IOException {
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);
    when(request.getParameter(REQUEST_PARAM_KEY_CODE)).thenReturn(TEST_AUTH_CODE);
    when(mockedUtil.getNewRefreshToken(TEST_AUTH_CODE)).thenReturn(TEST_REFRESH_TOKEN);

    servlet.setUserService(mockedUserService);
    servlet.setUtilObj(mockedUtil);
    servlet.setDataObj(mockedDataObj);
    servlet.doPost(request, response);
    
    verify(mockedUserService, times(1)).isUserLoggedIn();
    verify(mockedUserService, times(1)).getCurrentUser();
    verify(request, times(1)).getParameter(REQUEST_PARAM_KEY_CODE);
    verify(mockedUtil, times(1)).getNewRefreshToken(TEST_AUTH_CODE);
    verify(mockedUtil, times(1)).associateRefreshToken(TEST_USER_ID, TEST_REFRESH_TOKEN);
    verify(response).sendRedirect(HOME_URL);  
  }

  @Test
  public void userLoggedInNoAuthCode() throws IOException {
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);
    when(request.getParameter(REQUEST_PARAM_KEY_CODE)).thenReturn(null);

    servlet.setUserService(mockedUserService);
    servlet.setUtilObj(mockedUtil);
    servlet.doPost(request, response);
  
    verify(response).sendRedirect(LOGIN_URL);  
    verify(mockedUserService, times(1)).isUserLoggedIn();
    verify(mockedUserService, times(1)).getCurrentUser();
    verify(request, times(1)).getParameter(REQUEST_PARAM_KEY_CODE);
  }

  @Test
  public void userLoggedRefreshTokenFails() throws IOException {
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);
    when(mockedUserService.getCurrentUser()).thenReturn(userFake);
    when(request.getParameter(REQUEST_PARAM_KEY_CODE)).thenReturn(TEST_AUTH_CODE);
    when(mockedUtil.getNewRefreshToken(TEST_AUTH_CODE)).thenThrow(IOException.class);

    servlet.setUserService(mockedUserService);
    servlet.setUtilObj(mockedUtil);
    servlet.setDataObj(mockedDataObj);
    servlet.doPost(request, response);
    
    verify(mockedUserService, times(1)).isUserLoggedIn();
    verify(mockedUserService, times(1)).getCurrentUser();
    verify(request, times(1)).getParameter(REQUEST_PARAM_KEY_CODE);
    verify(mockedUtil, times(1)).getNewRefreshToken(TEST_AUTH_CODE);
    verify(response).sendRedirect(AUTHORIZE_URL);  
  }

}
