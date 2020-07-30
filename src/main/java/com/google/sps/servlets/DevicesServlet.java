package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.servlets.Util;
import com.google.sps.gson.Json;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

@WebServlet("/devices")
public class DevicesServlet extends HttpServlet {

  private final Gson GSON_OBJECT = new Gson();

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
    final String json = GSON_OBJECT.toJson(allDevices);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

  }

}