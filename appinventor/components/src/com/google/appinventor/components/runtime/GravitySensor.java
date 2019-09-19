// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

/**
 * Component providing data from the device's gravity sensor.
 */
@DesignerComponent(version = YaVersion.GRAVITY_COMPONENT_VERSION,
    description = "<p>Non-visible component.The gravity sensor provides a three dimensional vector indicating the direction and " +
            "magnitude of gravity. Typically, this sensor is used to determine the device's relative orientation in space</p>",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/gravitysensor.png")

@SimpleObject
public class GravitySensor extends AndroidNonvisibleComponent
    implements SensorEventListener, Deleteable, OnPauseListener, OnResumeListener {

  // Properties
  private boolean enabled;
  private float xAccel;
  private float yAccel;
  private float zAccel;

  // Sensor information
  private final SensorManager sensorManager;
  private final Sensor gravitySensor;
  private boolean listening;

  /**
   * Creates a new GravitySensor component.
   */
  public GravitySensor(ComponentContainer container) {
    super(container.$form());

    // Get sensors, and start listening.
    sensorManager = (SensorManager) form.getSystemService(Context.SENSOR_SERVICE);
    gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

    // Begin listening in onResume() and stop listening in onPause().
    form.registerForOnResume(this);
    form.registerForOnPause(this);

    // Set default property values.
    Enabled(true);
  }

  private void startListening() {
    if (!listening) {
      sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
      listening = true;
    }
  }

  private void stopListening() {
    if (listening) {
      sensorManager.unregisterListener(this);
      listening = false;

      // Throw out sensor information that will go stale.
      xAccel = 0.0f;
      yAccel = 0.0f;
      zAccel = 0.0f;
    }
  }

  // Events

  /**
   * GravityChanged event handler.
   */
  @SimpleEvent(description = "Indicates that the gravity sensor data has changed. The " +
          " timestamp parameter is the time in nanoseconds at which the event occurred. ")
  public void GravityChanged(
      float xAccel, float yAccel, float zAccel, long timestamp) {
    EventDispatcher.dispatchEvent(this, "GravityChanged",
        xAccel, yAccel, zAccel, timestamp);
  }

  // Properties

  /**
   * Available property getter method (read-only property).
   *
   * @return {@code true} indicates that a gravity sensor is available,
   *         {@code false} that it isn't
   */
  @SimpleProperty(description = "Indicates whether a gravity sensor is available.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Available() {
    return sensorManager.getSensorList(Sensor.TYPE_GRAVITY).size() > 0;
  }

  /**
   * Enabled property getter method.
   *
   * @return {@code true} indicates that the sensor generates events,
   *         {@code false} that it doesn't
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return enabled;
  }

  /**
   * Enabled property setter method.
   *
   * @param enabled  {@code true} enables sensor event generation,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(description = "If enabled, then sensor events will be generated and " +
      "XAngularVelocity, YAngularVelocity, and ZAngularVelocity properties will have " +
      "meaningful values.")
  public void Enabled(boolean enabled) {
    if (this.enabled != enabled) {
      this.enabled = enabled;
      if (enabled) {
        startListening();
      } else {
        stopListening();
      }
    }
  }

  /**
   * XAngularVelocity property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.</p>
   *
   * @return  current angular velocity around x axis
   */
  @SimpleProperty(description = "Force of gravity along the x axis",
      category = PropertyCategory.BEHAVIOR)
  public float GravityX() {
    return xAccel;
  }

  /**
   * YAngularVelocity property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.</p>
   *
   * @return  current angular velocity around y axis
   */
  @SimpleProperty(description = "Force of gravity along the y axis",
      category = PropertyCategory.BEHAVIOR)
  public float GravityY() {
    return yAccel;
  }

  /**
   * ZAngularVelocity property getter method (read-only property).
   *
   * <p>To return meaningful values the sensor must be enabled.</p>
   *
   * @return  current angular velocity around z axis
   */
  @SimpleProperty(description = "Force of gravity along the z axis",
      category = PropertyCategory.BEHAVIOR)
  public float GravityZ() {
    return zAccel;
  }

  // SensorListener implementation

  /**
   * Responds to changes in the gravity sensors.
   *
   * @param sensorEvent an event from the gravity sensor
   */
  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    if (enabled) {

      xAccel = (float) Math.toDegrees(sensorEvent.values[0]);
      yAccel = (float) Math.toDegrees(sensorEvent.values[1]);
      zAccel = (float) Math.toDegrees(sensorEvent.values[2]);

      // Raise event.
      GravityChanged(xAccel, yAccel, zAccel,
          sensorEvent.timestamp);
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    stopListening();
  }

  // OnPauseListener implementation

  public void onPause() {
    stopListening();
  }

  // OnResumeListener implementation

  public void onResume() {
    if (enabled) {
      startListening();
    }
  }
}
