package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.data.ListDeviceResponse;
import com.google.sps.gson.Json;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import java.security.GeneralSecurityException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.HttpUrl;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.File;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;

@WebServlet("/devices")
public class DevicesServlet extends HttpServlet {

  private final Gson GSON_OBJECT = new Gson();
  private final String CLIENT_SECRET_FILE = "/client_info.json";
  private final OkHttpClient client = new OkHttpClient();
  private final String INVALID_ACCESS_TOKEN = "INVALIDDD";
  private final String EMPTY_REFRESH_TOKEN = "";
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
        response.sendRedirect("/login");
    }
    final String userId = userService.getCurrentUser().getUserId();
    System.out.println("current user id is :" + userId);
    Query query = new Query("RefreshToken").setFilter(FilterOperator.EQUAL.of("userId", userId));
    PreparedQuery results = datastore.prepare(query);
    String refreshToken = EMPTY_REFRESH_TOKEN;
    for (final Entity entity : results.asIterable()) {
      refreshToken = (String) entity.getProperty("refreshToken");
      break;
    }
    if (refreshToken == EMPTY_REFRESH_TOKEN) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
    File file = new File(this.getClass().getResource(CLIENT_SECRET_FILE).getFile());
    final GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(
            JacksonFactory.getDefaultInstance(), new FileReader(file));
    final String clientId = clientSecrets.getDetails().getClientId();
    final String clientSecret = clientSecrets.getDetails().getClientSecret();
    final String accessToken = getAccessToken(refreshToken, clientId, clientSecret);
    HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.googleapis.com/admin/directory/v1/customer/my_customer/devices/chromeos").newBuilder();
    urlBuilder.addQueryParameter("maxResults", "55");
    urlBuilder.addQueryParameter("projection", "FULL");
    urlBuilder.addQueryParameter("sortOrder", "ASCENDING");
    urlBuilder.addQueryParameter("key", "AIzaSyBq4godZxCMXHkkqLDSve1x27gCSYmBfVM");
    String myUrl = urlBuilder.build().toString();
    Request req = new Request.Builder()
        .url(myUrl).addHeader("Authorization", "Bearer " + accessToken)
        .build();
    Response myResponse = client.newCall(req).execute();
    final String content = myResponse.body().string();
    response.setContentType("application/json");
    final String json = GSON_OBJECT.toJson(content);
    response.getWriter().println(json);
    System.out.println("SUCCESS!!!\n\n\n\n\n");
    // final List<ChromeOSDevice> allDevices = new ArrayList<>();
    // ListDeviceResponse resp = (ListDeviceResponse) Json.fromJson(content, ListDeviceResponse.class);
    // allDevices.addAll(resp.getDevices());
    // while (resp.hasNextPageToken()) {
    //     urlBuilder = HttpUrl.parse("https://www.googleapis.com/admin/directory/v1/customer/my_customer/devices/chromeos").newBuilder();
    //     urlBuilder.addQueryParameter("maxResults", "55");
    //     urlBuilder.addQueryParameter("projection", "FULL");
    //     urlBuilder.addQueryParameter("sortOrder", "ASCENDING");
    //     urlBuilder.addQueryParameter("key", "AIzaSyBq4godZxCMXHkkqLDSve1x27gCSYmBfVM");
    //     System.out.println((String) resp.getNextPageToken());
    //     urlBuilder.addQueryParameter("pageToken", (String) resp.getNextPageToken());
    //     String newUrl = urlBuilder.build().toString();
    //     Request newReq = new Request.Builder()
    //         .url( newUrl).addHeader("Authorization", "Bearer " + accessToken)
    //         .build();
    //     Response newResponse = client.newCall(newReq).execute();
    //     final String newContent = newResponse.body().string();
    //     resp = (ListDeviceResponse) Json.fromJson(newContent, ListDeviceResponse.class);
    //     allDevices.addAll(resp.getDevices());
    //     System.out.println(allDevices.size());
    // }
  }


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

  }

  private String getAccessToken(String refreshToken, String clientId, String clientSecret) throws IOException {
    try {
        GoogleTokenResponse response =
            new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                refreshToken, clientId, clientSecret).execute();
      System.out.println("Access token gotten: " + response.getAccessToken());
      return response.getAccessToken();
    } catch (TokenResponseException e) {
      if (e.getDetails() != null) {
        System.out.println(e.getMessage());
      }
    }
    return INVALID_ACCESS_TOKEN;
  }

}