package com.google.sps.servlets;
 
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.gson.Json;
import com.google.sps.servlets.Util;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.StringBuilder;
import java.security.GeneralSecurityException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
 
@WebServlet("/csv")
public class CSVServlet extends HttpServlet {
 
  private UserService userService = UserServiceFactory.getUserService();
  private Util utilObj = new Util();
  public final String LOGIN_URL = "/login";
  public final String HOME_URL = "/index.html";
 
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final User currentUser = userService.getCurrentUser();
    if ((!userService.isUserLoggedIn()) || (currentUser == null)) {
      response.sendRedirect(LOGIN_URL);
      return;
    }
    final String userId = currentUser.getUserId();
    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"my_devices.csv\"");
    try {
      OutputStream outputStream = response.getOutputStream();
      final List<ChromeOSDevice> allDevices = utilObj.getAllDevices(userId);
      final String outputResult = stringifyDevicesList(allDevices);
      outputStream.write(outputResult.getBytes());
      outputStream.flush();
      outputStream.close();
      response.sendRedirect(HOME_URL);
    } catch(Exception e) {
      System.out.println(e.toString());
      response.sendRedirect(LOGIN_URL);
    }
  }
 
  private String stringifyDevicesList(final List<ChromeOSDevice> allDevices) {
    final String heading = "DEVICE ID,SERIAL NUMBER,ANNOTATED ASSET ID,ANNOTATED LOCATION,ANNOTATED USER\n";
    final StringBuilder devicesStringBuilder = new StringBuilder(heading);
    for (final ChromeOSDevice device : allDevices) {
      devicesStringBuilder.append(device.getCSVLine());
    }
    return devicesStringBuilder.toString();
  }
 
  public void setUserService(UserService newUserService) {
    this.userService = newUserService;
  }
  
  public void setUtilObj(Util util) {
    this.utilObj = util;
  }
 
}
