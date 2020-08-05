package com.google.sps.servlets;

import org.apache.commons.io.FileUtils;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.data.ListDeviceResponse;
import com.google.sps.gson.Json;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.security.GeneralSecurityException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.util.ArrayList;
import java.util.List;

class Util {

  private static final String CLIENT_SECRET_FILE = "/client_info.json";
  private static final String API_KEY_FILE = "/api_key.txt";
  private static final OkHttpClient client = new OkHttpClient();
  private static final String INVALID_ACCESS_TOKEN = "INVALID";
  private static final String EMPTY_REFRESH_TOKEN = "";
  private static final String EMPTY_API_KEY = "";
  private static final String EMPTY_PAGE_TOKEN = "";
  private static final String ALL_DEVICES_ENDPOINT = "https://www.googleapis.com/admin/directory/v1/customer/my_customer/devices/chromeos";
  private static final String DEFAULT_MAX_DEVICES = "200"; //is limited to effectively 200
  private static final String DEFAULT_SORT_ORDER = "ASCENDING";
  private static final String DEFAULT_PROJECTION = "FULL";
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final String API_KEY = getAPIKey(); 

  public static List<ChromeOSDevice> getAllDevices(String userId) throws IOException {
    final String accessToken = getAccessToken(userId);
    ListDeviceResponse resp = getDevicesResponse(EMPTY_PAGE_TOKEN, accessToken);
    final List<ChromeOSDevice> allDevices = new ArrayList<>(resp.getDevices());
    while (resp.hasNextPageToken()) {
      final String pageToken = (String) resp.getNextPageToken();
      resp = getDevicesResponse(pageToken, accessToken);
      allDevices.addAll(resp.getDevices());
    }
    return allDevices;
  }

  public static String getAPIKey() {
    try {
      File file = new File(Util.class.getResource(API_KEY_FILE).getFile());
      String str = FileUtils.readFileToString(file);
      return str;
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return EMPTY_API_KEY;
  }

  private static String getRefreshToken(String userId) throws IOException {
    Query query = new Query("RefreshToken").setFilter(FilterOperator.EQUAL.of("userId", userId));
    PreparedQuery results = datastore.prepare(query);
    System.out.println(results.countEntities());
    try {
      Entity entity = results.asSingleEntity();
      String refreshToken = (String) entity.getProperty("refreshToken");
      return refreshToken;
    } catch (PreparedQuery.TooManyResultsException e) {
      throw new IOException("Error while getting refresh token");
    }
  }

  public static String getAccessToken(String userId) throws IOException {
    try {
      final String refreshToken = getRefreshToken(userId);
      File file = new File(Util.class.getResource(CLIENT_SECRET_FILE).getFile());
      final GoogleClientSecrets clientSecrets =
      GoogleClientSecrets.load(
        JacksonFactory.getDefaultInstance(), new FileReader(file));
      final String clientId = clientSecrets.getDetails().getClientId();
      final String clientSecret = clientSecrets.getDetails().getClientSecret();
      GoogleTokenResponse response =
      new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
        refreshToken, clientId, clientSecret).execute();
      return response.getAccessToken();
    } catch (TokenResponseException e) {
      if (e.getDetails() != null) {
        System.out.println(e.getMessage());
      }
    }
    return INVALID_ACCESS_TOKEN;
  }

  private static ListDeviceResponse getDevicesResponse(String pageToken, String accessToken) throws IOException {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(ALL_DEVICES_ENDPOINT).newBuilder();
    urlBuilder.addQueryParameter("maxResults", DEFAULT_MAX_DEVICES);
    urlBuilder.addQueryParameter("projection", DEFAULT_PROJECTION);
    urlBuilder.addQueryParameter("sortOrder", DEFAULT_SORT_ORDER);
    urlBuilder.addQueryParameter("key", API_KEY);
    if (!pageToken.equals(EMPTY_PAGE_TOKEN)) {
      urlBuilder.addQueryParameter("pageToken", pageToken);
    }
    final String myUrl = urlBuilder.build().toString();
    Request req = new Request.Builder().url(myUrl).addHeader("Authorization", "Bearer " + accessToken).build();
    Response myResponse = client.newCall(req).execute();
    final String content = myResponse.body().string();
    ListDeviceResponse resp = (ListDeviceResponse) Json.fromJson(content, ListDeviceResponse.class);
    return resp;
  }

}