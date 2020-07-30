package com.google.sps.servlets;

import com.google.sps.data.AnnotatedField;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.data.ListDeviceResponse;
import com.google.sps.servlets.Util;
import com.google.sps.gson.Json;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that aggregates chrome devices by a given field */
@WebServlet("/aggregate")
public class AggregationServlet extends HttpServlet {

  // Used for testing
  public AggregationServlet() {}

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    AnnotatedField field = null;
    try {
      field = AnnotatedField.create(request.getParameter("aggregationField"));
    } catch (IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().println(e.getMessage());
      return;
    }

    List<ChromeOSDevice> devices = amassDevices();
    Map<String, Integer> data = processData(devices, field);

    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().println(Json.toJson(data));
  }

  public List<ChromeOSDevice> amassDevices() throws IOException {
    List<ChromeOSDevice> devices = new ArrayList<>();
    final UserService userService = UserServiceFactory.getUserService();
    final User currentUser = userService.getCurrentUser();
    if ((!userService.isUserLoggedIn()) || (currentUser == null)) {
        throw new IOException("user is not logged in");
    }
    final String userId = currentUser.getUserId();
    final List<ChromeOSDevice> allDevices = Util.getAllDevices(userId);
    return allDevices;
  }

  public Map<String, Integer> processData(List<ChromeOSDevice> devices, AnnotatedField field) {
    Map<String, Integer> counts = new HashMap<>();

    for (ChromeOSDevice device : devices) {
      String fieldValue = field.getField(device);
      Integer newVal = counts.getOrDefault(fieldValue, new Integer(0)).intValue() + 1;
      counts.put(fieldValue, newVal);
    }

    return counts;
  }
}
