package com.google.sps.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
  private final String TEST_USER_ID = "testUserId";

  @Test
  public void userNotLoggedIn() throws IOException {
    User mockedUser = mock(User.class);
    when(mockedUser.getUserId()).thenReturn(TEST_USER_ID);

    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(false);
    when(mockedUserService.getCurrentUser()).thenReturn(mockedUser);
    
    servlet.setUserService(mockedUserService);
    servlet.doGet(request, response);

    verify(response).sendRedirect(LOGIN_URL);
    verify(mockedUser, times(1)).getUserId();
    verify(mockedUserService, times(1)).isUserLoggedIn();
  }


}
