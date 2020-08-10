package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.gson.Json;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/status")
public class StatusServlet extends HttpServlet {

  private UserService userService = UserServiceFactory.getUserService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final boolean userLoggedIn = userService.isUserLoggedIn();
    response.setContentType("application/json");
    final String json = Json.toJson(userLoggedIn);
    response.getWriter().println(json);
  }

  public void setUserService(UserService newUserService) {
    this.userService = newUserService;
  }
  
}
