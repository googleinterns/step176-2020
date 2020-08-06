package com.google.sps.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.ChromeOSDevice;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.sps.gson.Json;
import com.google.sps.servlets.Util;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.json.simple.JSONArray;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/update")
public class UpdateServlet extends HttpServlet {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final Gson GSON_OBJECT = new Gson();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    final User currentUser = userService.getCurrentUser();
    if ((!userService.isUserLoggedIn()) || (currentUser == null)) {
      response.sendRedirect("/login");
      return;
    }
    Map<String, String> mp = new HashMap<>();
    if (request.getParameterMap().containsKey("AnnotatedLocation")) {
        mp.put("AnnotatedLocation", (String) request.getParameter("AnnotatedLocation"));
    }
    if (request.getParameterMap().containsKey("AnnotatedAssetId")) {
        mp.put("AnnotatedAssetId", (String) request.getParameter("AnnotatedAssetId"));
    }
    if (request.getParameterMap().containsKey("AnnotatedUser")) {
        mp.put("AnnotatedUser", (String) request.getParameter("AnnotatedUser"));
    }
    final String rawDeviceIds = (String) request.getParameter("deviceIds");
    Type listType = new TypeToken<List<String>>() {}.getType();
    final List<String> deviceIds = GSON_OBJECT.fromJson(rawDeviceIds, listType);
    System.out.println(deviceIds);
    System.out.println(mp);
    final String userId = currentUser.getUserId();
    final String accessToken = Util.getAccessToken(userId);
    List<String> locations = Arrays.asList("NYC", "SF", "LA", "BOS", "DC");
    List<String> users = Arrays.asList("Bob", "Alice", "Eve", "george", "michael", "blab", "name");
    for (int i = 0; i < deviceIds.size(); i++) {
        updateDevice(accessToken, (String) deviceIds.get(i), mp);
    }
    response.sendRedirect("/index.html");
    return;
  }

  private void updateDevice(String accessToken, String deviceId, Map<String, String> mp) throws IOException {
    final String myUrl = getUpdateUrl(deviceId);
    OkHttpClient client = new OkHttpClient();
    String json = getJsonFromMap(mp);
    RequestBody body = RequestBody.create(JSON, json);
    Request req = new Request.Builder().url(myUrl).put(body).addHeader("Authorization", "Bearer " + accessToken).build();
    Response myResponse = client.newCall(req).execute();
    myResponse.body().close();
    System.out.println("finished updating " + deviceId);
  }

  private String getJsonFromMap(Map<String, String> mp) {
        final String json = GSON_OBJECT.toJson(mp);
        return json;
  }

  private String getUpdateUrl(String deviceId) {
      return "https://www.googleapis.com/admin/directory/v1/customer/my_customer/devices/chromeos/" + deviceId + "?projection=BASIC";
  }

}