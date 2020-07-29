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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;

@WebServlet("/authorize")
public class AuthorizeServlet extends HttpServlet {

  private final Gson GSON_OBJECT = new Gson();
  private final String CLIENT_SECRET_FILE = "/client_info.json";
  private final OkHttpClient client = new OkHttpClient();
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

  }


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
        response.sendRedirect("/login");
    }
    final String authCode = (String) request.getParameter("code");
    Entity tokenEntity = new Entity("RefreshToken");
    final String userId = userService.getCurrentUser().getUserId();
    final String refreshToken = getRefreshCode(authCode);
    tokenEntity.setProperty("userId", userId);
    tokenEntity.setProperty("refreshToken", refreshToken);
    System.out.println("got the fresh token of: " + refreshToken);
    datastore.put(tokenEntity);
    response.sendRedirect("/index.html");
  }

  private String getRefreshCode(String authCode) throws IOException {
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
                "https://oauth2.googleapis.com/token",
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret(),
                authCode,
                "http://localhost:8080") 
                .execute();
    final String refreshToken = tokenResponse.getRefreshToken();
    return refreshToken;
  }

}