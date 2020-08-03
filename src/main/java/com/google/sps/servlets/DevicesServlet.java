package com.google.sps.servlets;

import java.util.ArrayList;
import com.google.sps.data.ChromeOSDevice;
import java.security.GeneralSecurityException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.google.sps.gson.Json;
import java.util.List;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;import com.google.sps.servlets.Util;
import javax.servlet.annotation.WebServlet;

@WebServlet("/devices")
public class DevicesServlet extends HttpServlet {


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    final User currentUser = userService.getCurrentUser();
    if ((!userService.isUserLoggedIn()) || (currentUser == null)) {
      response.sendRedirect("/login");
      return;
    }
    final String userId = currentUser.getUserId();
    final List<ChromeOSDevice> allDevices = Util.getAllDevices(userId);
    response.setContentType("application/json");
    final String json = Json.toJson(allDevices);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

  }

}