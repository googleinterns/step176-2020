package com.google.sps.servlets;

import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
// import com.google.api.services.admin.directory.Directory;
// import com.google.api.services.admin.directory.DirectoryScopes;
// import com.google.api.services.admin.directory.model.User;
// import com.google.api.services.admin.directory.model.Users;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@WebServlet("/token")
public class TokenServlet extends HttpServlet {

  private final Gson GSON_OBJECT = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authCode = (String) request.getParameter("code");
    String CLIENT_SECRET_FILE = "/path/to/client_secret.json";

    System.out.println("fin");
    // GoogleClientSecrets clientSecrets =
    //     GoogleClientSecrets.load(
    //         JacksonFactory.getDefaultInstance(), new FileReader(CLIENT_SECRET_FILE));
            
    GoogleTokenResponse tokenResponse =
            new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                "934172118901-btsopome05u5a6ma1rfkde5p12015th1.apps.googleusercontent.com", //clientSecrets.getDetails().getClientId(),
                "L0RvHrl9GHbl5cU6tbIPtOjt", //clientSecrets.getDetails().getClientSecret(),
                authCode,
                "http://localhost:8080") 
                .execute();

    String accessToken = tokenResponse.getAccessToken();

    System.out.println(accessToken);
    System.out.println("is the access token!");
  }

}