package com.google.sps.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.reflect.TypeToken;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.gson.Json;
import com.google.sps.servlets.Util;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/update")
public class UpdateServlet extends HttpServlet {

  // public static final Gson GSON_OBJECT = new Gson();
  private static final List<String> relevantFields = Arrays.asList("annotatedLocation", "annotatedAssetId", "annotatedUser");
  private UserService userService = UserServiceFactory.getUserService();
  private Util utilObj = new Util();
  public String LOGIN_URL = "/login";
  public String INDEX_URL = "/index.html";
  public String DEVICE_IDS_PARAMETER_NAME = "deviceIds";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final User currentUser = userService.getCurrentUser();
    if ((!userService.isUserLoggedIn()) || (currentUser == null)) {
      response.sendRedirect(LOGIN_URL);
      return;
    }
    final String userId = currentUser.getUserId();
    final Map<String, String> updatesToMake = new HashMap<>();
    for (final String fieldToUpdate : relevantFields) {
      final String fieldContent = request.getParameter(fieldToUpdate);
      if (fieldContent != null) {
        updatesToMake.put(fieldToUpdate, fieldContent);
      }
    }
    final String updatesInJson = getJsonFromMap(updatesToMake);
    final String relevantDeviceIds = (String) request.getParameter(DEVICE_IDS_PARAMETER_NAME);
    final List<String> deviceIds = getDeviceIds(relevantDeviceIds);
    if (!deviceIds.isEmpty() && !updatesToMake.isEmpty()) {
      utilObj.updateDevices(userId, deviceIds, updatesInJson);
    }
    response.sendRedirect(INDEX_URL);
    return;
  }

  private List<String> getDeviceIds(String relevantDeviceIds) {
    final Type listType = new TypeToken<List<String>>() {}.getType();
    final List<String> deviceIds = Json.fromJson(relevantDeviceIds, listType);
    return deviceIds;
  } 

  private String getJsonFromMap(Map<String, String> mp) {
    final String json = Json.toJson(mp);
    return json;
  }

  public void setUserService(UserService newUserService) {
    this.userService = newUserService;
  }
  
  public void setUtilObj(Util util) {
    this.utilObj = util;
  }

}