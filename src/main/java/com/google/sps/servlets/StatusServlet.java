package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;getUserId()
import javax.servlet.http.HttpServletResponse;

@WebServlet("/status")
public class StatusServlet extends HttpServlet {

  private final Gson GSON_OBJECT = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    final boolean userLoggedIn = userService.isUserLoggedIn();
    response.setContentType("application/json");
    final String json = GSON_OBJECT.toJson(userLoggedIn);
    response.getWriter().println(json);
  }
  
}
