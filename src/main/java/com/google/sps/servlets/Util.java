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
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;


class Util {

    private static final Gson GSON_OBJECT = new Gson();
    private static final String CLIENT_SECRET_FILE = "/client_info.json";
    private static final OkHttpClient client = new OkHttpClient();
    private static final String INVALID_ACCESS_TOKEN = "INVALIDDD";
    private static final String EMPTY_REFRESH_TOKEN = "";
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public static List<ChromeOSDevice> getAllDevices(String userId) throws IOException {
    System.out.println("current user id is :" + userId);
    Query query = new Query("RefreshToken").setFilter(FilterOperator.EQUAL.of("userId", userId));
    PreparedQuery results = datastore.prepare(query);
    String refreshToken = EMPTY_REFRESH_TOKEN;
    for (final Entity entity : results.asIterable()) {
      refreshToken = (String) entity.getProperty("refreshToken");
      break;
    }
    if (refreshToken == EMPTY_REFRESH_TOKEN) {
        throw new IOException("no refresh token!!");
    }
    File file = new File(Util.class.getResource(CLIENT_SECRET_FILE).getFile());
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
    ListDeviceResponse resp = (ListDeviceResponse) Json.fromJson(content, ListDeviceResponse.class);
    final List<ChromeOSDevice> allDevices = new ArrayList<>();
    allDevices.addAll(resp.getDevices());
    while (resp.hasNextPageToken()) {
        urlBuilder = HttpUrl.parse("https://www.googleapis.com/admin/directory/v1/customer/my_customer/devices/chromeos").newBuilder();
        urlBuilder.addQueryParameter("maxResults", "55");
        urlBuilder.addQueryParameter("projection", "FULL");
        urlBuilder.addQueryParameter("sortOrder", "ASCENDING");
        urlBuilder.addQueryParameter("key", "AIzaSyBq4godZxCMXHkkqLDSve1x27gCSYmBfVM");
        System.out.println((String) resp.getNextPageToken());
        urlBuilder.addQueryParameter("pageToken", (String) resp.getNextPageToken());
        String newUrl = urlBuilder.build().toString();
        Request newReq = new Request.Builder()
            .url( newUrl).addHeader("Authorization", "Bearer " + accessToken)
            .build();
        Response newResponse = client.newCall(newReq).execute();
        final String newContent = newResponse.body().string();
        resp = (ListDeviceResponse) Json.fromJson(newContent, ListDeviceResponse.class);
        allDevices.addAll(resp.getDevices());
        System.out.println(allDevices.size());
    }
    return allDevices;
    }

    private static String getAccessToken(String refreshToken, String clientId, String clientSecret) throws IOException {
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