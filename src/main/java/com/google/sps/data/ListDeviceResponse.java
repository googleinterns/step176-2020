package com.google.sps.data;

import com.google.sps.data.ChromeOSDevice;
import java.util.ArrayList;
import java.util.List;

/*
 * Class representing response to `list Chrome OS Devices` request.
 * See https://developers.google.com/admin-sdk/directory/v1/reference/chromeosdevices/list
*/
public final class ListDeviceResponse {

  private final String kind;
  private final List<ChromeOSDevice> chromeosdevices;
  private final String nextPageToken;
  private final String etag;

  public ListDeviceResponse(String kind, List<ChromeOSDevice> chromeosdevices, String nextPageToken, String etag) {
    this.kind = kind;
    this.chromeosdevices = chromeosdevices;
    this.nextPageToken = nextPageToken;
    this.etag = etag;
  }

  public List<ChromeOSDevice> getDevices() {
    List<ChromeOSDevice> devices = new ArrayList<>(chromeosdevices);
    return chromeosdevices;
  }
}
