package com.google.sps.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.google.sps.gson.Json;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.servlet.annotation.WebServlet;

@WebServlet("/status")
public class StatusServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    final boolean userLoggedIn = userService.isUserLoggedIn();
    response.setContentType("application/json");
    final String json = Json.toJson(userLoggedIn);
    response.getWriter().println(json);
  }
  
}