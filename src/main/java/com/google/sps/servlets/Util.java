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

  //This class only works locally [endpoints will not function if deployed]
  private final String TOKEN_END_POINT = "https://oauth2.googleapis.com/token";
  private final String REROUTE_LINK = "http://localhost:8080";
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
  public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

  public List<ChromeOSDevice> getAllDevices(String userId) throws IOException, TokenResponseException, TooManyResultsException {
    final String apiKey = getAPIKey(); 
    final String accessToken = getAccessToken(userId);
    ListDeviceResponse resp = getDevicesResponse(EMPTY_PAGE_TOKEN, accessToken, apiKey);
    final List<ChromeOSDevice> allDevices = new ArrayList<>(resp.getDevices());
    while (resp.hasNextPageToken()) {
      final String pageToken = (String) resp.getNextPageToken();
      resp = getDevicesResponse(pageToken, accessToken, apiKey);
      allDevices.addAll(resp.getDevices());
    }
    return allDevices;
  }

  public static String getAPIKey() throws IOException {
    File file = new File(Util.class.getResource(API_KEY_FILE).getFile());
    String str = FileUtils.readFileToString(file);
    return str;
  }

  private static String getRefreshToken(String userId) throws IOException, TooManyResultsException {
    Query query = new Query("RefreshToken").setFilter(FilterOperator.EQUAL.of("userId", userId));
    PreparedQuery results = datastore.prepare(query);
    System.out.println(results.countEntities());
    Entity entity = results.asSingleEntity();
    String refreshToken = (String) entity.getProperty("refreshToken");
    return refreshToken;
  }

  public static String getAccessToken(String userId) throws IOException, TokenResponseException, TooManyResultsException {
    final String refreshToken = getRefreshToken(userId);
    File file = new File(Util.class.getResource(CLIENT_SECRET_FILE).getFile());
    final GoogleClientSecrets clientSecrets =
    GoogleClientSecrets.load(
        JacksonFactory.getDefaultInstance(), new FileReader(file));
    final String clientId = clientSecrets.getDetails().getClientId();
    final String clientSecret = clientSecrets.getDetails().getClientSecret();
    GoogleTokenResponse response =
    new GoogleRefreshTokenRequest(
        new NetHttpTransport(), new JacksonFactory(), refreshToken, clientId, clientSecret)
        .execute();
    return response.getAccessToken();
  }

  private static ListDeviceResponse getDevicesResponse(String pageToken, String accessToken, String apiKey) throws IOException {
    HttpUrl.Builder urlBuilder = HttpUrl.parse(ALL_DEVICES_ENDPOINT).newBuilder();
    urlBuilder.addQueryParameter("maxResults", DEFAULT_MAX_DEVICES);
    urlBuilder.addQueryParameter("projection", DEFAULT_PROJECTION);
    urlBuilder.addQueryParameter("sortOrder", DEFAULT_SORT_ORDER);
    urlBuilder.addQueryParameter("key", apiKey);
    if (!pageToken.equals(EMPTY_PAGE_TOKEN)) {
      urlBuilder.addQueryParameter("pageToken", pageToken);
    }
    final String deviceResponseURL = urlBuilder.build().toString();
    Request req = new Request.Builder().url(deviceResponseURL).addHeader("Authorization", "Bearer " + accessToken).build();
    Response deviceResponse = client.newCall(req).execute();
    final String content = deviceResponse.body().string();
    ListDeviceResponse resp = (ListDeviceResponse) Json.fromJson(content, ListDeviceResponse.class);
    return resp;
  }

  public void deleteStaleTokens(String userId) {
    Query query = new Query("RefreshToken").setFilter(FilterOperator.EQUAL.of("userId", userId));
    PreparedQuery results = datastore.prepare(query);
    List<Key> keysToDelete = new ArrayList<>();
    for (final Entity entity : results.asIterable()) {
      final long id = entity.getKey().getId();
      final Key key = KeyFactory.createKey("RefreshToken", id);
      keysToDelete.add(key);
    }
    datastore.delete(keysToDelete);
  }

  public String getNewRefreshToken(String authCode) throws IOException {
    File file = new File(this.getClass().getResource(CLIENT_SECRET_FILE).getFile());
    final GoogleClientSecrets clientSecrets =
    GoogleClientSecrets.load(
      JacksonFactory.getDefaultInstance(), new FileReader(file));
    final String clientId = clientSecrets.getDetails().getClientId();
    final String clientSecret = clientSecrets.getDetails().getClientSecret();
    final GoogleTokenResponse tokenResponse =
      new GoogleAuthorizationCodeTokenRequest(
        new NetHttpTransport(),
        JacksonFactory.getDefaultInstance(),
        TOKEN_END_POINT,
        clientSecrets.getDetails().getClientId(),
        clientSecrets.getDetails().getClientSecret(),
        authCode,
        REROUTE_LINK) 
        .execute();
    final String refreshToken = tokenResponse.getRefreshToken();
    return refreshToken;
  }

  public void associateRefreshToken(String userId, String refreshToken) {
    Entity tokenEntity = new Entity("RefreshToken");
    tokenEntity.setProperty("userId", userId);
    tokenEntity.setProperty("refreshToken", refreshToken);
    deleteStaleTokens(userId);
    datastore.put(tokenEntity);
  }
 
  public void updateDevices(String userId, List<String> deviceIds, String updatesInJson) throws IOException {
    final String accessToken = getAccessToken(userId);
    for (final String deviceId : deviceIds) {
      final String updateURL = getUpdateUrl(deviceId);
      RequestBody body = RequestBody.create(JSON_TYPE, updatesInJson);
      Request req = new Request.Builder().url(updateURL).put(body).addHeader("Authorization", "Bearer " + accessToken).build();
      Response updateResponse = client.newCall(req).execute();
      updateResponse.body().close();
    }
  }
  
  private String getUpdateUrl(String deviceId) {
      return "https://www.googleapis.com/admin/directory/v1/customer/my_customer/devices/chromeos/" + deviceId + "?projection=BASIC";
  }

}