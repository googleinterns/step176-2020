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
public final class LoginServletTest {

  private LoginServlet servlet = new LoginServlet();
  private HttpServletRequest request = mock(HttpServletRequest.class);
  private HttpServletResponse response = mock(HttpServletResponse.class);
  private final String LOGOUT_URL = "LOG OUT";
  private final String LOGIN_URL = "LOG IN";

  @Test
  public void userLoggedIn() throws IOException {
    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);
    when(mockedUserService.createLogoutURL("/index.html")).thenReturn(LOGOUT_URL);
    
    servlet.setUserService(mockedUserService);
    servlet.doGet(request, response);

    verify(response).setContentType("text/html");
    verify(response).sendRedirect(LOGOUT_URL);
    verify(mockedUserService, times(1)).isUserLoggedIn();
    verify(mockedUserService, times(1)).createLogoutURL("/index.html");
  }

  @Test
  public void userNotLoggedIn() throws IOException {
    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(false);
    when(mockedUserService.createLoginURL("/index.html")).thenReturn(LOGIN_URL);
    
    servlet.setUserService(mockedUserService);
    servlet.doGet(request, response);

    verify(response).setContentType("text/html");
    verify(response).sendRedirect(LOGIN_URL);
    verify(mockedUserService, times(1)).isUserLoggedIn();
    verify(mockedUserService, times(1)).createLoginURL("/index.html");
  }


}
