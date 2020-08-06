package com.google.sps.servlets;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.AggregationResponse;
import com.google.sps.data.AnnotatedField;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.data.ListDeviceResponse;
import com.google.sps.gson.Json;
import com.google.sps.servlets.Util;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Servlet that aggregates chrome devices by a given field */
@WebServlet("/aggregate")
public class AggregationServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    LinkedHashSet<AnnotatedField> fields = null;
    try {
      fields = getAggregationFields(request);
    } catch (IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().println(e.getMessage());
      return;
    }

    try {
      List<ChromeOSDevice> devices = amassDevices();
      MultiKeyMap<String, List<String>> data = processData(devices, fields);
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().println(Json.toJson(new AggregationResponse(data, fields)));
    } catch (IOException e) {
      response.sendRedirect("/login");
      return;
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().println(e.getMessage());
      return;
    }
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

  public static MultiKeyMap<String, List<String>> processData(
      List<ChromeOSDevice> devices,
      LinkedHashSet<AnnotatedField> fields) {
    MultiKeyMap<String, List<String>> counts = new MultiKeyMap<>();

    for (ChromeOSDevice device : devices) {
      String keyParts[] = new String[fields.size()];
      Iterator<AnnotatedField> it = fields.iterator();
      int i = 0;

      while (it.hasNext()) {
        keyParts[i++] = it.next().getField(device);
      }

      MultiKey key = new MultiKey(keyParts);
      List<String> newVal = counts.getOrDefault(key, new ArrayList<String>());
      newVal.add(device.getDeviceId());

      counts.put(key, newVal);
    }

    return counts;
  }

  public static LinkedHashSet<AnnotatedField> getAggregationFields(HttpServletRequest request) {
    String fieldString = request.getParameter("aggregationField");
    if (fieldString == null) {
      throw new IllegalArgumentException("Aggregation field cannot be null");
    }

    LinkedHashSet<AnnotatedField> aggregationFields = new LinkedHashSet<>();
    String fieldNames[] = fieldString.split(",");
    for (int i = 0; i < fieldNames.length; i++) {
      aggregationFields.add(AnnotatedField.create(fieldNames[i]));
    }

    return aggregationFields;
  }
}
