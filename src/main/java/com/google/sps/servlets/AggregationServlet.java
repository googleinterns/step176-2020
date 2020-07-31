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

import com.google.sps.data.AggregationResponse;
import com.google.sps.data.AnnotatedField;
import com.google.sps.data.ChromeOSDevice;
import com.google.sps.data.ListDeviceResponse;
import com.google.sps.gson.Json;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

/** Servlet that aggregates chrome devices by a given field */
@WebServlet("/aggregate")
public class AggregationServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    Set<AnnotatedField> fields = null;
    try {
      fields = getAggregationFields(request);
    } catch (IllegalArgumentException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().println(e.getMessage());
      return;
    }

    List<ChromeOSDevice> devices = amassDevices();
    MultiKeyMap<String, Integer> data = processData(devices, fields);

    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().println(Json.toJson(new AggregationResponse(data, fields)));
  }

  public List<ChromeOSDevice> amassDevices() {
    List<ChromeOSDevice> devices = new ArrayList<>();

    // TODO: Send requests to API to get all devices; requires OAuth

    // Some mock devices so we have data in the interim
    devices.add(new ChromeOSDevice("assetId", "location", "user", "deviceId", "serialNumber"));
    devices.add(new ChromeOSDevice("12345", "California", "Jane", "ae25f1-91ce6a", "SN54321"));
    devices.add(new ChromeOSDevice("12345", "New Jersey", "Jane", "<deviceId>", "SN12345"));

    return devices;
  }

  public static MultiKeyMap<String, Integer> processData(List<ChromeOSDevice> devices, Set<AnnotatedField> fields) {
    MultiKeyMap<String, Integer> counts = new MultiKeyMap<>();

    for (ChromeOSDevice device : devices) {
      String keyParts[] = new String[fields.size()];
      Iterator<AnnotatedField> it = fields.iterator();
      int i = 0;

      while (it.hasNext()) {
        keyParts[i++] = it.next().getField(device);
      }

      MultiKey key = new MultiKey(keyParts);
      Integer newVal = counts.getOrDefault(key, new Integer(0)).intValue() + 1;

      counts.put(key, newVal);
    }

    return counts;
  }

  public static Set<AnnotatedField> getAggregationFields(HttpServletRequest request) {
    String fieldString = request.getParameter("aggregationField");
    if (fieldString == null) {
      throw new IllegalArgumentException("Aggregation field cannot be null");
    }

    Set<AnnotatedField> aggregationFields = new HashSet<>();
    String fieldNames[] = fieldString.split(",");
    for (int i = 0; i < fieldNames.length; i++) {
      aggregationFields.add(AnnotatedField.create(fieldNames[i]));
    }

    return aggregationFields;
  }
}
