package com.google.sps.servlets;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Test the status servlet.
 */
@RunWith(JUnit4.class)
public final class StatusServletTest {

  private StatusServlet servlet = new StatusServlet();
  private HttpServletRequest request = mock(HttpServletRequest.class);
  private HttpServletResponse response = mock(HttpServletResponse.class);

  @Test
  public void userLoggedIn() throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(true);

    servlet.setUserService(mockedUserService);
    servlet.doGet(request, response);

    String result = stringWriter.getBuffer().toString().trim();
    String expected = "true";

    verify(response).setContentType("application/json");
    Assert.assertEquals(expected, result);
  }

  @Test
  public void userNotLoggedIn() throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    UserService mockedUserService = mock(UserService.class);
    when(mockedUserService.isUserLoggedIn()).thenReturn(false);

    servlet.setUserService(mockedUserService);
    servlet.doGet(request, response);

    String result = stringWriter.getBuffer().toString().trim();
    String expected = "false";

    verify(response).setContentType("application/json");
    Assert.assertEquals(expected, result);
  }

}
