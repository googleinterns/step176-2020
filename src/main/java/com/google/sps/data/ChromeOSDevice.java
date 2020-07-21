package com.google.sps.data;

/*
 * Class representing a Chrome OS Device.
 * See https://developers.google.com/admin-sdk/directory/v1/reference/chromeosdevices
 * This class is an incomplete list of all chrome os device properties, instead containing
 * only the subset of properties related to the webapp.  These properties can be expanded
 * if the scope changes.
 */
public class ChromeOSDevice {

  private String annotatedAssetId;
  private String annotatedLocation;
  private String annotatedUser;

  private final String deviceId;
  private final String serialNumber;

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

    sanitize();
  }

  // The Gson deserializer doesnt actually call the object's constructor, so we defined a custom
  // deserializer which calls the sanitize() method. To keep everything consistent, the
  // ChromeOSDevice constructor also calls the sanitize() method so the same standard is held
  public void sanitize() {
    annotatedAssetId = annotatedAssetId == null ? "" : annotatedAssetId;
    annotatedLocation = annotatedLocation == null ? "" : annotatedLocation;
    annotatedUser = annotatedUser == null ? "" : annotatedUser;
  }

  public String getAnnotatedField(String fieldName) throws IllegalArgumentException {
    switch(fieldName) {
      case "annotatedAssetId":
        return annotatedAssetId;
      case "annotatedLocation":
        return annotatedLocation;
      case "annotatedUser":
        return annotatedUser;
      default:
        throw new IllegalArgumentException(fieldName + "is not a valid annotated field");
    }
  }

  public String getDeviceId() {
    return deviceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof ChromeOSDevice)) {
      return false;
    }

    ChromeOSDevice other = (ChromeOSDevice) obj;
    return annotatedAssetId.equals(other.annotatedAssetId) &&
        annotatedLocation.equals(other.annotatedLocation) &&
        annotatedUser.equals(other.annotatedUser) &&
        deviceId.equals(other.deviceId) &&
        serialNumber.equals(other.serialNumber);
  }
}
