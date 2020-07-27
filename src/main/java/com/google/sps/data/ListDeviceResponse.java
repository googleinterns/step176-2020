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
  private final List<ChromeOSDevice> chromeOSdevices;
  private final String nextPageToken;
  private final String etag;

  public ListDeviceResponse(String kind, List<ChromeOSDevice> chromeOSdevices, String nextPageToken, String etag) {
    this.kind = kind;
    this.chromeOSdevices = getDeviceListCopy(chromeOSdevices);
    this.nextPageToken = nextPageToken;
    this.etag = etag;
  }
  
  public boolean hasNextPageToken() {
      return nextPageToken != null;
  }

  public String getNextPageToken() {
      return nextPageToken;
  }

  public List<ChromeOSDevice> getDeviceListCopy(List<ChromeOSDevice> original) {
    final List<ChromeOSDevice> devices = new ArrayList<>();
    for (final ChromeOSDevice device : original) {
      devices.add(device.copy());
    }
    return devices;
  }

  public List<ChromeOSDevice> getDevices() {
    return getDeviceListCopy(chromeOSdevices);
  }
}
