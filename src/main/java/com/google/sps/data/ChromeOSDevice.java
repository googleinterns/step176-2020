package com.google.sps.data;

/*
 * Class representing a Chrome OS Device.
 * See https://developers.google.com/admin-sdk/directory/v1/reference/chromeosdevices
 * This class is an incomplete list of all chrome os device properties, instead containing
 * only the subset of properties related to the webapp.  These properties can be expanded
 * if the scope changes.
 * ChromeOSDevice should not be mutated.
 */
public class ChromeOSDevice {

  private String annotatedAssetId;
  private String annotatedLocation;
  private String annotatedUser;
  private final String deviceId;
  private final String serialNumber;
  private final String status;

  public ChromeOSDevice(
      String annotatedAssetId,
      String annotatedLocation,
      String annotatedUser,
      String deviceId,
      String serialNumber) {
    this.annotatedAssetId = annotatedAssetId;
    this.annotatedLocation = annotatedLocation;
    this.annotatedUser = annotatedUser;
    this.deviceId = deviceId;
    this.serialNumber = serialNumber;
    this.status = "Provisioned";
    sanitize();
  }

  // The Gson deserializer doesn't actually call the object's constructor, so we defined a custom
  // deserializer which calls the sanitize() method. To keep everything consistent, the
  // ChromeOSDevice constructor also calls the sanitize() method so the same standard is held
  public void sanitize() {
    annotatedAssetId = (annotatedAssetId == null) ? "" : annotatedAssetId;
    annotatedLocation = (annotatedLocation == null) ? "" : annotatedLocation;
    annotatedUser = (annotatedUser == null) ? "" : annotatedUser;
  }

  public ChromeOSDevice copy() {
    return new ChromeOSDevice(
        annotatedAssetId,
        annotatedLocation,
        annotatedUser,
        deviceId,
        serialNumber);
  }

  public String getDeviceId() {
    return deviceId;
  }


  public String getSerialNumber() {
    return serialNumber;
  }


  public String getAnnotatedAssetId() {
    return annotatedAssetId;
  }


  public String getAnnotatedLocation() {
    return annotatedLocation;
  }


  public String getAnnotatedUser() {
    return annotatedUser;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ChromeOSDevice)) {
      return false;
    }
    return comparable((ChromeOSDevice) obj);
  }

  private boolean comparable(ChromeOSDevice other) {
    return annotatedAssetId.equals(other.getAnnotatedAssetId()) &&
           annotatedLocation.equals(other.getAnnotatedLocation()) &&
           annotatedUser.equals(other.getAnnotatedUser()) &&
           deviceId.equals(other.getDeviceId()) &&
           serialNumber.equals(other.getSerialNumber());
  }

  @Override
  public int hashCode() {
    final String hashString = deviceId + "|" + serialNumber;
    return hashString.hashCode();
  }

  @Override
  public String toString() {
    return "[Device with ID: " + deviceId + "]";
  }

  public String getCSVLine() {
    return deviceId + "," + serialNumber + "," + annotatedAssetId + ","  + annotatedLocation + ","  + annotatedUser + "\n";  
  }

}
