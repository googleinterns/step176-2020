package com.google.sps.servlets;

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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@WebServlet("/authorize")
public class AuthorizeServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private UserService userService = UserServiceFactory.getUserService();
  private Util utilObj = new Util();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final User currentUser = userService.getCurrentUser();
    // System.out.println("a");
    final String authCode = (String) request.getParameter("code");
    // System.out.println(authCode);


    //     Enumeration enumeration = request.getParameterNames();
    //     while (enumeration.hasMoreElements()) {
    //         String parameterName = (String) enumeration.nextElement();
    //         System.out.println("Parameter = " + parameterName);
    //     }
    // System.out.println("done enumerating");



    if ((!userService.isUserLoggedIn()) || (currentUser == null) || (authCode == null)) {
      response.sendRedirect("/login");
      return;
    }
    final String userId = currentUser.getUserId();
    try {
      final String refreshToken = utilObj.getNewRefreshToken(authCode);
      utilObj.associateRefreshToken(userId, refreshToken);
      System.out.println("success");
      response.sendRedirect("/index.html");
    } catch (IOException e) {
      response.sendRedirect("/authorize");
    }
  }

  public void setUserService(UserService newUserService) {
    this.userService = newUserService;
  }
  
  public void setUtilObj(Util util) {
    this.utilObj = util;
  }

  public void setDataObj(DatastoreService dataObj) {
    this.datastore = dataObj;
  }

}