// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.res.AssetManager;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.VectorDrawable;
import android.widget.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.IllegalArgumentError;
import com.google.appinventor.components.runtime.util.*;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;

/**
 * Component for displaying images and animations.
 *
 */
@DesignerComponent(version = YaVersion.IMAGE_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Component for displaying images.  The picture to display, " +
    "and other aspects of the Image's appearance, can be specified in the " +
    "Designer or in the Blocks Editor.")
@SimpleObject
@UsesAssets(fileNames = "star.png,octagon.png,triangle.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class Image extends AndroidViewComponent implements View.OnClickListener, View.OnLongClickListener {

  private final ImageView view;

  private String picturePath = "";  // Picture property

  private double rotationAngle = 0.0;

  private int scalingMode = Component.SCALING_SCALE_PROPORTIONALLY;
  private String imageEffect = "";
  private String shape = "";

  /**
   * Creates a new Image component.
   *
   * @param container  container, component will be placed in
   */
  public Image(ComponentContainer container) {
    super(container);

    view = new ImageView(container.$context()) {
      @Override
      public boolean verifyDrawable(Drawable dr) {
        super.verifyDrawable(dr);
        // TODO(user): multi-image animation
        return true;
      }
    };
    view.setFocusable(true);
    Enabled(true);
    view.setOnClickListener(this);
    view.setOnLongClickListener(this);

    // Adds the component to its designated container
    container.$add(this);
  }

  @Override
  public View getView() {
    return view;
  }

  public ImageView getImageView() {
    return view;
  }

  /**
   * Returns the path of the image's picture.
   *
   * @return  the path of the image's picture
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Picture() {
    return picturePath;
  }

  /**
   * Specifies the path of the image's picture.
   *
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path of the image's picture
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  @SimpleProperty
  public void Picture(String path) {
    picturePath = (path == null) ? "" : path;

    // reset to original picture path
    Drawable drawable;
    try {
      drawable = MediaUtil.getBitmapDrawable(container.$form(), picturePath);
    } catch (IOException ioe) {
      Log.e("Image", "Unable to load " + picturePath);
      drawable = null;
    }

    ViewUtil.setImage(view, drawable);

    // we need to ensure picture path isn't empty, otherwise, we get error below. For above, its okay, it'll just reset to no picture
    if (picturePath.isEmpty()) return;
    // now set effect, if any
    setEffect();
    setShape();
  }


  @SimpleFunction(description = "Applies different effects to Image. Valid effects are: invert, binary, blue, brown, bw, green, pink, red, sepia")
  public void ImageEffect(String effect) {
    //restore to original picture
    this.imageEffect = effect;
    Picture(picturePath);
  }

  @SimpleFunction(description = "Transforms the image to specified shape. Valid shapes are: circle, star, triangle, octagon")
  public void ApplyShape(String shape) {
    shape = shape.toLowerCase().trim();
    this.shape = shape;
    Picture(picturePath);
  }

  private Bitmap transformImage(Bitmap original, Bitmap mask) {
    Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
    int size = Math.max(original.getWidth(), original.getHeight());
    Bitmap maskNew = Bitmap.createScaledBitmap(mask, original.getWidth(), original.getHeight(), true);
    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);

    // Draw the original bitmap (DST during Porter-Duff transfer)
    canvas.drawBitmap(original, 0, 0, null);


    // DST_IN = Whatever was there, keep the part that overlaps
    // with what I'm drawing now
    Paint maskPaint = new Paint();
    maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
//    maskPaint.setStrokeWidth(container.$form().convertDpToDensity(55));
//    maskPaint.setColor(Color.RED);
    canvas.drawBitmap(maskNew, 0, 0, maskPaint);

    return bitmap;
  }

  public Bitmap getRoundedCornerBitmap() {
    Bitmap original = ((BitmapDrawable)view.getDrawable()).getBitmap();

    Bitmap output = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
    android.graphics.Canvas canvas = new android.graphics.Canvas(output);

    final int color = 0xff424242;
    Paint paint = new Paint();
    Rect rect = new Rect(0, 0, original.getWidth(), original.getHeight());
    RectF rectF = new RectF(rect);
    float roundPx = Math.min(original.getWidth(), original.getHeight()) * .8f;

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(original, rect, rect, paint);

    return output;
  }


  private void setEffect() {
    try {
      Bitmap bitmap = new ShapeUtil().convertToBitmap(view.getDrawable());
      switch (imageEffect.toLowerCase().trim()) {
        case "invert":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.INVERT));
          break;
        case "binary":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.BINARY));
          break;
        case "blue":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.BLUE));
          break;
        case "brown":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.BROWN));
          break;
        case "bw":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.BW));
          break;
        case "green":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.GREEN));
          break;
        case "pink":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.PINK));
          break;
        case "sepia":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.SEPIA));
          break;
        case "red":
          view.setImageBitmap(new ShapeUtil().setImageEffect(bitmap, ShapeStyle.RED));
          break;
        default:
          // do nothing
      }
    } catch (Exception e) {
      //no-op
    }
  }

  private void setShape() {
    if (this.shape.isEmpty()) return;

    try {
      Bitmap mask;
      switch (shape) {
        case "octagon":
          mask = getBitmapFromAsset("component/octagon.png");
          break;
        case "star":
          mask = getBitmapFromAsset("component/star.png");
          break;
        case "triangle":
          mask = getBitmapFromAsset("component/triangle.png");
          break;
        case "circle":
          view.setImageBitmap(getRoundedCornerBitmap());
          return;
        default:
          //todo:
          return;
      }

      Bitmap original = ((BitmapDrawable)view.getDrawable()).getBitmap();
      Bitmap transformed = transformImage(original, mask);
      view.setImageBitmap(transformed);
    } catch (Exception e) {
      // no-op
    }

  }
  public Bitmap getBitmapFromAsset(String filePath) {
    AssetManager assetManager = container.$context() .getAssets();

    InputStream istr;
    Bitmap bitmap = null;
    try {
      istr = assetManager.open(filePath);
      bitmap = BitmapFactory.decodeStream(istr);
    } catch (IOException e) {
      // handle exception
    }

    return bitmap;
  }




  /**
   * Specifies the angle at which the image picture appears rotated.
   *
   * @param rotated  the rotation angle
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0.0")
  @SimpleProperty
  public void RotationAngle(double rotationAngle) {
    if (this.rotationAngle == rotationAngle) {
      return;                   // Nothing to do...
                                // This also means that you can always set the
                                // the angle to 0.0 even on older Android devices
    }
    if (SdkLevel.getLevel() < SdkLevel.LEVEL_HONEYCOMB) {
      container.$form().dispatchErrorOccurredEvent(this, "RotationAngle",
        ErrorMessages.ERROR_IMAGE_CANNOT_ROTATE);
      return;
    }
    HoneycombUtil.viewSetRotate(view, rotationAngle);
    this.rotationAngle = rotationAngle;
  }

  @SimpleProperty(description = "The angle at which the image picture appears rotated. " +
      "This rotation does not appear on the designer screen, only on the device.",
      category = PropertyCategory.APPEARANCE)
  public double RotationAngle() {
    return rotationAngle;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    defaultValue = "False")
  // @Deprecated -- We will deprecate this in a future release (jis: 2/12/2016)
  @SimpleProperty(description = "Specifies whether the image should be resized to match the size of the ImageView.")
  public void ScalePictureToFit(boolean scale) {
    if (scale)
      view.setScaleType(ImageView.ScaleType.FIT_XY);
    else
      view.setScaleType(ImageView.ScaleType.FIT_CENTER);
  }

  /**
   * Animation property setter method.
   *
   * @see AnimationUtil
   *
   * @param animation  animation kind
   */
  @SimpleProperty(description = "This is a limited form of animation that can attach " +
      "a small number of motion types to images.  The allowable motions are " +
      "ScrollRightSlow, ScrollRight, ScrollRightFast, ScrollLeftSlow, ScrollLeft, " +
      "ScrollLeftFast, Stop and HyperJump",
      category = PropertyCategory.APPEARANCE)
  // TODO(user): This should be changed from a property to an "animate" method, and have the choices
  // placed in a dropdown.  Aternatively the whole thing should be removed and we should do
  // something that is more consistent with sprites.
  public void Animation(String animation) {
    AnimationUtil.ApplyAnimation(view, animation, container.$context());
  }

  @Deprecated
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_SCALING,
//      defaultValue = Component.SCALING_SCALE_PROPORTIONALLY + "")
  @SimpleProperty(description = "This property determines how the picture " +
      "scales according to the Height or Width of the Image. Scale " +
      "proportionally (0) preserves the picture aspect ratio. Scale to fit " +
      "(1) matches the Image area, even if the aspect ratio changes.")
  public void Scaling(int mode) {
    switch (mode) {
      case 0:
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        break;
      case 1:
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        break;
      default:
        throw new IllegalArgumentError("Illegal scaling mode: " + mode);
    }
    scalingMode = mode;
  }

  @SimpleProperty
  public int Scaling() {
    return scalingMode;
  }

  @Override
  public void onClick(View view) {
    Click();
  }

  @Override
  public boolean onLongClick(View view) {
    return LongClick();
  }

  /**
   * Indicates a user has clicked on the button.
   */
  @SimpleEvent(description = "User tapped and released the component.")
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }

  /**
   * Indicates a user has long clicked on the button.
   */
  @SimpleEvent(description = "User held the component down.")
  public boolean LongClick() {
    return EventDispatcher.dispatchEvent(this, "LongClick");
  }

  /**
   * Returns true if the component is clickable.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return view.isEnabled();
  }

  /**
   * Specifies whether the checkbox should be active and clickable.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty(description = "Can be used make component Clickable")
  public void Enabled(boolean enabled) {
    getView().setEnabled(enabled);
  }

}
