// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.sps.data.ChromeOSDevice;
import com.google.sps.data.ListDeviceResponse;
import com.google.sps.gson.Json;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that aggregates chrome devices by a given field */
@WebServlet("/aggregate")
public class AggregationServlet extends HttpServlet {

  private static final Set<String> aggregableFields =
      new HashSet<>(Arrays.asList("annotatedAssetId, annotatedLocation, annotatedUser"));
  
  // Used for testing
  public AggregationServlet() {}

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    String aggregationField = request.getParameter("aggregationField");
    if (!isValidField(aggregationField)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().println(aggregationField + " is not a valid aggregation field");
      return;
    }

    List<ChromeOSDevice> devices = amassDevices();
    Map<String, Integer> data = processData(devices, aggregationField);

    response.getWriter().println(Json.toJson(data));
  }

  public List<ChromeOSDevice> amassDevices() {
    List<ChromeOSDevice> devices = new ArrayList<>();

    // TODO: Send requests to API to get all devices; requires OAuth

    // Some mock devices so we have data in the interim
    devices.add(new ChromeOSDevice("assetId", "location", "user", "deviceId", "serialNumber"));
    devices.add(new ChromeOSDevice("12345", "California", "Jane", "ae25f1-91ce6a", "SN54321"));

    return devices;
  }

  public Map<String, Integer> processData(List<ChromeOSDevice> devices, String aggregationField) {
    Map<String, Integer> counts = new HashMap<>();

    for (ChromeOSDevice device : devices) {
      String fieldValue = device.getAnnotatedField(aggregationField);
      Integer newVal = counts.getOrDefault(fieldValue, new Integer(0)).intValue() + 1;
      counts.put(fieldValue, newVal);
    }

    return counts;
  }

  private boolean isValidField(String field) {
    return aggregableFields.contains(field);
  }
}
