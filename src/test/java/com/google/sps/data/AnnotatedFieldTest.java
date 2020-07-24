package com.google.sps.data;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/*
 * Ensure that the AnnotatedField.create() method throws the correct errors when
 * given illegal input.  Test that the getField() method works correctly.
 */
@RunWith(JUnit4.class)
public final class AnnotatedFieldTest {

  @Test(expected = IllegalArgumentException.class)
  public void invalidFieldName() {
    AnnotatedField.create("serialNumber");
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullFieldName() {
    AnnotatedField.create(null);
  }

  @Test
  public void properFieldName() {
    AnnotatedField field = AnnotatedField.create("annotatedLocation");

    Assert.assertEquals(AnnotatedField.LOCATION, field);
  }

  @Test
  public void getsCorrectField() {
    AnnotatedField field = AnnotatedField.create("annotatedUser");

    ChromeOSDevice device = new ChromeOSDevice("assetId", "location", "user", "deviceId", "serialNumber");

    Assert.assertEquals(device.getAnnotatedUser(), field.getField(device));
  }

}
