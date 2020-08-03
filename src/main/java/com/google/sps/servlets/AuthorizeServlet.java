package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.File;
import com.google.api.client.util.store.FileDataStoreFactory;
import java.io.FileReader;
import java.security.GeneralSecurityException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.client.http.HttpTransport;
import java.io.IOException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;import javax.servlet.annotation.WebServlet;

@WebServlet("/authorize")
public class AuthorizeServlet extends HttpServlet {

  private final String TOKEN_END_POINT = "https://oauth2.googleapis.com/token";
  private final String REROUTE_LINK = "http://localhost:8080";
  private final String CLIENT_SECRET_FILE = "/client_info.json";
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final UserService userService = UserServiceFactory.getUserService();
    final User currentUser = userService.getCurrentUser();
    if ((!userService.isUserLoggedIn()) || (currentUser == null)) {
      response.sendRedirect("/login");
      return;
    }
    final String userId = currentUser.getUserId();
    final String authCode = (String) request.getParameter("code");
    Entity tokenEntity = new Entity("RefreshToken");
    final String refreshToken = getRefreshCode(authCode);
    tokenEntity.setProperty("userId", userId);
    tokenEntity.setProperty("refreshToken", refreshToken);
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
        TOKEN_END_POINT,
        clientSecrets.getDetails().getClientId(),
        clientSecrets.getDetails().getClientSecret(),
        authCode,
        REROUTE_LINK) 
        .execute();
    final String refreshToken = tokenResponse.getRefreshToken();
    return refreshToken;
  }

}