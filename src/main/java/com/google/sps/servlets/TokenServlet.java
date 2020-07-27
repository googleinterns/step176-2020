package com.google.sps.servlets;

import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
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

@WebServlet("/token")
public class TokenServlet extends HttpServlet {

  private final Gson GSON_OBJECT = new Gson();
  private final String CLIENT_SECRET_FILE = "/client_info.json";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

  }


  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authCode = (String) request.getParameter("code");
    
    File file = new File(this.getClass().getResource(CLIENT_SECRET_FILE).getFile());

    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(
            JacksonFactory.getDefaultInstance(), new FileReader(file));
            
    GoogleTokenResponse tokenResponse =
            new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret(),
                authCode,
                "http://localhost:8080") 
                .execute();

    String accessToken = tokenResponse.getAccessToken();

    System.out.println(accessToken);
    System.out.println("is the access token!");

    OkHttpClient client = new OkHttpClient();
    HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.googleapis.com/admin/directory/v1/customer/my_customer/devices/chromeos").newBuilder();
    urlBuilder.addQueryParameter("maxResults", "3");
    urlBuilder.addQueryParameter("projection", "FULL");
    urlBuilder.addQueryParameter("sortOrder", "ASCENDING");
    urlBuilder.addQueryParameter("key", "AIzaSyBq4godZxCMXHkkqLDSve1x27gCSYmBfVM");
    String myUrl = urlBuilder.build().toString();
    System.out.println(myUrl);
    Request req = new Request.Builder()
        .url(myUrl).addHeader("Authorization", "Bearer " + accessToken)
        .build();
    Response myResponse = client.newCall(req).execute();
    final String content  =myResponse.body().string();
    try {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(content);
        JSONObject mainResponseJSON = (JSONObject) obj;
        System.out.println(mainResponseJSON.get("chromeosdevices"));
    } catch(ParseException pe) {
		
         System.out.println("position: " + pe.getPosition());
         System.out.println(pe);
    }



  }

}