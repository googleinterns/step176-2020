package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.gson.Json;
import com.google.sps.servlets.Util;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/csv")
public class CSVServlet extends HttpServlet {

  private UserService userService = UserServiceFactory.getUserService();
  private Util utilObj = new Util();
  public final String LOGIN_URL = "/login";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final User currentUser = userService.getCurrentUser();
    if ((!userService.isUserLoggedIn()) || (currentUser == null)) {
      response.sendRedirect(LOGIN_URL);
      return;
    }
    final String userId = currentUser.getUserId();
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"userDirectory.csv\"");
    try {
      OutputStream outputStream = response.getOutputStream();
      String outputResult = "xxxx, yyyy, zzzz, aaaa, bbbb, ccccc, dddd, eeee, ffff, gggg\n";
      outputStream.write(outputResult.getBytes());
      outputStream.flush();
      outputStream.close();
    } catch(Exception e) {
      System.out.println(e.toString());
    }
  }

  public void setUserService(UserService newUserService) {
    this.userService = newUserService;
  }
  
  public void setUtilObj(Util util) {
    this.utilObj = util;
  }

}