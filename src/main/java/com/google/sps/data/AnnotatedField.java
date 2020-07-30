package com.google.sps.data;

public enum AnnotatedField {
  ASSET_ID {
    public String getField(ChromeOSDevice device) {
      return device.getAnnotatedAssetId();
    }
  },
  LOCATION {
    public String getField(ChromeOSDevice device) {
      return device.getAnnotatedLocation();
    }
  },
  USER {
    public String getField(ChromeOSDevice device) {
      return device.getAnnotatedUser();
    }
  };

  public abstract String getField(ChromeOSDevice device);


  public static AnnotatedField create(String fieldName) {
    if (fieldName == null) {
      throw new IllegalArgumentException("Annotated field name cannot be null.");
    }

    switch (fieldName) {
      case "annotatedAssetId":
        return AnnotatedField.ASSET_ID;
      case "annotatedLocation":
        return AnnotatedField.LOCATION;
      case "annotatedUser":
        return AnnotatedField.USER;
      default:
        throw new IllegalArgumentException("Invalid annotated field name: '" + fieldName + "'");
    }
  }
}
