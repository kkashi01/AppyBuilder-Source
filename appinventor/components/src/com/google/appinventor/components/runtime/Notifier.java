// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.appybuilder.utils.dialogs.Animation;
import com.appybuilder.utils.dialogs.FancyAlertDialog;
import com.appybuilder.utils.dialogs.FancyAlertDialogListener;
import com.appybuilder.utils.dialogs.Icon;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.appybuilder.utils.styledtoast.StyledToast;

import java.io.IOException;

/**
 * The Notifier component displays alert messages and creates Android log entries through
 * the following methods:
 * <ul>
 * <li> ShowMessageDialog: user must dismiss the message by pressing a button.
 * <li> ShowChooseDialog: displays two buttons to let the user choose one of two responses,
 *      for example, yes or no, after which the AfterChoosing event is raised.
 * <li> ShowTextDialog: lets the user enter text in response to the message, after
 *      which the AfterTextInput event is raised.
 * <li> ShowAlert: displays an alert that goes away by itself after
 *      a short time.
 * <li> ShowProgressDialog: displays an alert with a loading spinner (or horizontal line) that cannot be dismissed by
 *      the user. Can only be dismissed by using the DismissProgressDialog block.
 * <li> DismissProgressDialog: Dismisses the progress dialog displayed by ShowProgressDialog.
 * <li> LogError: logs an error message to the Android log.
 * <li> LogInfo: logs an info message to the Android log.
 * <li> LogWarning: logs a warning message to the Android log.
 * </ul>
 *
 * @author halabelson@google.com (Hal Abelson)
 */

@DesignerComponent(version = YaVersion.NOTIFIER_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "The Notifier component displays alert dialogs, messages, and temporary alerts, " +
        "and creates Android log entries through the following methods: " +
        "<ul>" +
        "<li> ShowMessageDialog: displays a message which the user must dismiss by pressing a button.</li>" +
        "<li> ShowChooseDialog: displays a message two buttons to let the user choose one of two responses, " +
        "for example, yes or no, after which the AfterChoosing event is raised.</li>" +
        "<li> ShowTextDialog: lets the user enter text in response to the message, after " +
        "which the AfterTextInput event is raised. " +
        "<li> ShowAlert: displays a temporary  alert that goes away by itself after a short time.</li>" +
        "<li> ShowProgressDialog: displays an alert with a loading spinner that cannot be dismissed by " +
        "the user. It can only be dismissed by using the DismissProgressDialog block.</li>" +
        "<li> DismissProgressDialog: Dismisses the progress dialog displayed by ShowProgressDialog.</li>" +
        "<li> LogError: logs an error message to the Android log. </li>" +
        "<li> LogInfo: logs an info message to the Android log.</li>" +
        "<li> LogWarning: logs a warning message to the Android log.</li>" +
        "<li>The messages in the dialogs (but not the alert) can be formatted using the following HTML tags:" +
        "&lt;b&gt;, &lt;big&gt;, &lt;blockquote&gt;, &lt;br&gt;, &lt;cite&gt;, &lt;dfn&gt;, &lt;div&gt;, " +
        "&lt;em&gt;, &lt;small&gt;, &lt;strong&gt;, &lt;sub&gt;, &lt;sup&gt;, &lt;tt&gt;. &lt;u&gt;</li>" +
        "<li>You can also use the font tag to specify color, for example, &lt;font color=\"blue\"&gt;.  Some of the " +
        "available color names are aqua, black, blue, fuchsia, green, grey, lime, maroon, navy, olive, purple, " +
        "red, silver, teal, white, and yellow</li>" +
        "</ul>",
    nonVisible = true,
    iconName = "images/notifier.png")
@SimpleObject
//Don't use it here because Notifer is also called from Form. We are including it in the form
//@UsesLibraries(libraries = "styledtoast.aar,fancydialog.aar")
public final class Notifier extends AndroidNonvisibleComponent implements Component {

  private static final String LOG_TAG = "Notifier";
  private final Activity activity;
  private final Handler handler;
  protected final ComponentContainer container;

  private ProgressDialog progressDialog;

  //Length of Notifier message display
  private int notifierLength = Component.TOAST_LENGTH_LONG;

  // Notifier background color
  private int backgroundColor = Color.DKGRAY;

  // Notifier text color
  private int textColor = Color.WHITE;
  private boolean linkify=true;
  private static Bitmap titleBitMap=null;
  private String picturePath = "";  // Picture property


  /**
   * Creates a new Notifier component.
   *
   * @param container the enclosing component
   */
  public Notifier (ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    this.container = container;

    handler = new Handler();
    progressDialog = null;
  }

  /**
   * Display a progress dialog that cannot be dismissed by the user. To dismiss
   * this alert, you must use the DismissProgressDialog block
   *
   * @param message the text in the alert box
   * @param title the title for the alert box
   */
  @SimpleFunction(description = "Shows a dialog box with an optional title and message "
    + "(use empty strings if they are not wanted). This dialog box contains a spinning or horizontal"
    + "artifact to indicate that the program is working. It cannot be canceled by the user "
    + "but must be dismissed by the AppyBuilder Program by using the DismissProgressDialog "
    + "block. progressStyle can be 0 or 1, where 0=Spinner and 1=Horizontal style")
  public void ShowProgressDialog(String message, String title, int progressStyle) {
    progressDialog(message, title, progressStyle);
  }

  /**
   * Dismisses the alert created by the ShowProgressDialog block
   */
  @SimpleFunction(description = "Dismiss a previously displayed ProgressDialog box")
  public void DismissProgressDialog() {
    if (progressDialog != null) {
      progressDialog.dismiss();
      progressDialog = null;
    }
  }

  /**
   * This method creates the actual ProgressDialog. If one is already being
   * displayed, then it dismisses it, and creates this new one.
   * @param message	the message for the dialog
   * @param title the title for the dialog
   */
  public void progressDialog(String message, String title, int style) {
    if (style == 1) {
      progressDialogHorizontal(message, title, 1);
      return;
    }

    if (progressDialog != null) {
      DismissProgressDialog();
    }

    progressDialog = new ProgressDialog(container.$context());

    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    if (Build.VERSION.SDK_INT >= 21 /* Lilipop */ ) {

      SpannableString ss1 = new SpannableString(title);
//    ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);  //2f means double the size
      ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss1.length(), 0);

      SpannableString ss2 = new SpannableString(message);
//    ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
      ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
      progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

      progressDialog.setCancelable(false);
      progressDialog.setTitle(ss1);
      progressDialog.setMessage(ss2);
    } else {
      progressDialog.setTitle(title);
      progressDialog.setMessage(message);
    }


    progressDialog.show();
  }

  public void progressDialogHorizontal(String message, String title, int style) {
    if (progressDialog != null) {
      DismissProgressDialog();
    }

    progressDialog = new ProgressDialog(container.$context());

    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

    // Progress Dialog Max Value
    progressDialog.setMax(100);

    // Incremented By Value 1
    progressDialog.incrementProgressBy(2);

    if (Build.VERSION.SDK_INT >= 21 /* Lilipop */ ) {

      SpannableString ss1 = new SpannableString(title);
//    ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length(), 0);  //2f means double the size
      ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss1.length(), 0);

      SpannableString ss2 = new SpannableString(message);
//    ss2.setSpan(new RelativeSizeSpan(2f), 0, ss2.length(), 0);
      ss2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss2.length(), 0);
      progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

      progressDialog.setTitle(ss1);
      progressDialog.setMessage(ss2);
    } else {
      progressDialog.setTitle(title);
      progressDialog.setMessage(message);
    }

    progressDialog.setCancelable(false);

//    progressDialog.getLayoutInflater()

    // remove the numbers being displayed:
    progressDialog.setIndeterminate(true); // this will disable showing the percent numbers
    progressDialog.setProgressNumberFormat(null);
    progressDialog.setProgressPercentFormat(null);


    progressDialog.show();

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // Here you should write your time consuming task...
          while (progressDialog.getProgress() <= progressDialog.getMax()) {
            Thread.sleep(200);
            handler.post(new Runnable() {
              public void run() {
                if (progressDialog != null) progressDialog.incrementProgressBy(2);
              }
            });

            if (progressDialog != null && progressDialog.getProgress() >= progressDialog.getMax()) {
              progressDialog.setProgress(10);
//              progressDialog.dismiss();
            }
          }
        } catch (Exception e) {
          //no-op
        }
      }
    }).start();
  }

  public void ShowMessageDialog(String message, String title, String buttonText) {
    ShowMessageDialog(message, title, buttonText, 1);
  }
  /**
   * Display an alert dialog with a single button that dismisses the alert.
   *
   * @param message the text in the alert box
   * @param title the title for the alert box
   * @param buttonText the text on the button
   */
  @SimpleFunction(description = "Shows a dialog box with one button. " +
          "The animationType can be a value from 1 to 4. Enter 5 for No Animation")
  public void ShowMessageDialog(String message, String title, final String buttonText, int animationType) {
//    oneButtonAlert(activity, message, title, buttonText);
    oneButtonDialog(activity,
            message,
            title,
            buttonText,
            new Runnable() {public void run() {AfterChoosing(buttonText);}}
            , animationType
            , linkify
    );
  }

  // This method is declared static, with an explicit activity input, so that other
  // components can use it
  public static void oneButtonAlert(Activity activity,String message, String title, String buttonText) {
    Runnable doNothing = new Runnable () {public void run() {}};

    // now calling the new dialog window
    oneButtonDialog(activity, message, title, buttonText, doNothing, 2, false);
  }

  // converts a string that includes HTML tags to a spannable string that can
  // be included in an alert
  private static SpannableString stringToHTML(String message) {
    return new SpannableString(Html.fromHtml(message));
  }

  // NOTE: THIS IS CALLED FROM PLAY APP. IT needs to have some animation type
  public void ShowChooseDialog(String message, String title, final String button1Text,
                               final String button2Text, boolean cancelable) {
    ShowChooseDialog(message, title, button1Text, button2Text, cancelable, 2);
  }
  /**
   * Displays an alert with two buttons that have specified text.  If cancelable is true,
   * there is an additional button marked CANCEL that cancels the dialog.
   * Raises the AfterChoosing event when the choice has been made, and returns the text of
   * the button that was pressed.
   *
   * @param message the text in the alert box
   * @param title the title for the alert box
   * @param button1Text the text on the left-hand button
   * @param button2Text the text on the right-hand button
   * @param cancelable indicates if additional CANCEL button should be added
   */
  @SimpleFunction(description = "Shows a dialog box with two buttons, from which the user can choose. "
      + " If cancelable is false, user cannot close dialog without a response"
      + "Pressing a button will raise the AfterChoosing event.  The \"choice\" parameter to AfterChoosing "
      + "will be the text on the button that was pressed. " +
          "The animationType can be a value from 1 to 4. Enter 5 for No Animation")
  public void ShowChooseDialog(String message, String title, final String button1Text,
      final String button2Text, boolean cancelable, int animationType) {
    twoButtonDialog(activity,
        message,
        title,
        button1Text,
        button2Text,
        cancelable,
        new Runnable() {public void run() {AfterChoosing(button1Text);}},
        new Runnable() {public void run() {AfterChoosing(button2Text);}},
        new Runnable() {public void run() {AfterChoosing(activity.getString(android.R.string.cancel));}}
        , animationType, linkify
        );
  }


  // This method takes three runnables that specify the actions to be performed
  // when the buttons are pressed.  It's declared static with an explicit activity input
  // so that other components can use it.
  public static void twoButtonDialog(final Activity activity, String message,  String title,
      final String button1Text,  final String button2Text, boolean cancelable,
      final Runnable positiveAction, final Runnable negativeAction, final Runnable cancelAction, final int animationType, final boolean linkify) {
    Log.i(LOG_TAG, "ShowChooseDialog: " + message);



    new FancyAlertDialog.Builder(activity)
            .setTitle(title)
//            .setBackgroundColor(Color.parseColor("#303F9F"))  //Don't pass R.color.colorvalue
            .setMessage(message)

//            .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
            .setPositiveBtnText(button1Text)

            .setNegativeBtnText(button2Text)
//            .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue

            .setAnimation(getAnimationType(animationType))
            .isCancellable(cancelable)
            .isLinkify(linkify)

            .setImageBitmap(titleBitMap, titleBitMap==null?Icon.Gone: Icon.Visible)
//            .setIcon(android.R.drawable.btn_star_big_on, Icon.Gone)

            .OnPositiveClicked(new FancyAlertDialogListener() {
              @Override
              public void OnClick() {
                positiveAction.run();
              }
            })
            .OnNegativeClicked(new FancyAlertDialogListener() {
              @Override
              public void OnClick() {
                negativeAction.run();
              }
            })
            .OnCancelClicked(new FancyAlertDialogListener() {
              @Override
              public void OnClick() {
                cancelAction.run();
              }
            })

            .build();

  }

  public static void oneButtonDialog(final Activity activity, String message,  String title,
      final String button1Text,
      final Runnable positiveAction, final int animationType, final boolean linkify) {
    Log.i(LOG_TAG, "ShowChooseDialog: " + message);

    new FancyAlertDialog.Builder(activity)
            .setTitle(title)
//            .setBackgroundColor(Color.parseColor("#303F9F"))  //Don't pass R.color.colorvalue
            .setMessage(message)


//            .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
            .setPositiveBtnText(button1Text)

            .setAnimation(getAnimationType(animationType))
            .isCancellable(false)
            .isLinkify(linkify)

            .setImageBitmap(titleBitMap, titleBitMap==null?Icon.Gone: Icon.Visible)
//            .setIcon(android.R.drawable.btn_star_big_on, Icon.Gone)

            .OnPositiveClicked(new FancyAlertDialogListener() {
              @Override
              public void OnClick() {
                positiveAction.run();
              }
            })

            .buildOneButton();
  }

  private static Animation getAnimationType(int animationType) {
    if (animationType==1) return Animation.POP;
    if (animationType==2) return Animation.SIDE_LEFT;
    else if (animationType==3) return Animation.SIDE_RIGHT;
    else if (animationType==4) return Animation.SLIDE;
    return Animation.NONE;
  }

  /**
   * Event after the user has made a selection for ShowChooseDialog.
   * @param choice is the text on the button the user pressed
   */
  @SimpleEvent
  public void AfterChoosing(String choice) {
    EventDispatcher.dispatchEvent(this, "AfterChoosing", choice);
  }

  /**
   * Shows a dialog box in which the user can enter text, after which the
   * AfterTextInput event is raised.
   * @param message the text in the alert box
   * @param title the title for the alert box
   * @param cancelable indicates whether the user should be able to cancel out of dialog.
   *                   When true, an additional CANCEL button will be added allowing user to cancel
   *                   out of dialog. If selected, it will raise AfterTextInput with text of CANCEL.
   */
  @SimpleFunction(description = "Shows a dialog box where the user can enter text, after which the "
     + "AfterTextInput event will be raised.  If cancelable is true there will be an additional CANCEL button. "
     + "Entering text will raise the AfterTextInput event.  The \"response\" parameter to AfterTextInput "
     + "will be the text that was entered, or \"Cancel\" if the CANCEL button was pressed.")

  public void ShowTextDialog(String message, String title, boolean cancelable) {
    textInputDialog(message, title, cancelable);
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty (description = "If set to true will attempt to make text clickable where possible; e.g. hyperlinks, phone numbers ")
  public void Linkify(boolean enabled) {
    this.linkify = enabled;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET, defaultValue = "")
  @SimpleProperty(description = "Specify an image to be displayed next to Title. If path is empty (or invalid), no image will be displayed")
  public void TitleImage(String path) {
    picturePath = (path == null) ? "" : path.trim();

    Drawable drawable;
    try {
      drawable = MediaUtil.getBitmapDrawable(container.$form(), picturePath);
      titleBitMap = convertToBitmap(drawable, 150,150);
    } catch (IOException ioe) {
      Log.e("Image", "Unable to load " + picturePath);
      titleBitMap = null;
    }
  }

  /**
   * Returns the path of the component's picture.
   *
   * @return  the path of the component's picture
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "Returns the path used for TitleImage")
  public String TitleImage() {
    return picturePath;
  }


  // https://msol.io/blog/android/android-convert-drawable-to-bitmap/
  private Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
    if (drawable == null) return null;
//    int size = container.$form().convertDpToDensity(150);
//    Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
//    Bitmap mutableBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    Bitmap mutableBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    android.graphics.Canvas canvas = new android.graphics.Canvas(mutableBitmap);
//    drawable.setBounds(0, 0, widthPixels, heightPixels);
    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    drawable.draw(canvas);

    return mutableBitmap;
  }

  @SimpleProperty
  public boolean Linkify() {
    return  this.linkify;
  }

  /**
   * Display an alert with a text entry. If cancelable is true, then also displays a "CANCEL"
   * button, allowing user to cancel out of dialog.
   * Raises the AfterTextInput event when the text has been entered and the user presses "OK".
   * Raises the AfterTextInput event when users presses CANCEL, passing the text "CANCEL" to AfterTextInput
   *
   * @param message the text in the alert box
   * @param title the title for the alert box
   * @param cancelable indicates whether the user should be able to cancel out of dialog.
   *                   When true, an additional CANCEL button will be added allowing user to cancel
   *                   out of dialog. On selection, will raise AfterTextInput with text of CANCEL.
   */
  // TODO(hal):  It would be cleaner to define this in terms of oneButtonAlert and generalize
  // oneButtonAlert so it can be used both for messages and text input.  We could have merged
  // this method into ShowTextDialog, but that would make it harder to do the generalization.
  private void textInputDialog(String message, String title, boolean cancelable) {
    if (Build.VERSION.SDK_INT >= 21 /* Lilipop */ ) {
      textInputDialogMaterial(message, title, cancelable);
      return;
    }

    SpannableString spannableString=null;
    if (linkify) {
      spannableString = new SpannableString(message);
      Linkify.addLinks(spannableString, Linkify.ALL);
    }

    final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    alertDialog.setTitle(title);


    if (linkify) {
      alertDialog.setMessage(spannableString);
    } else {
      alertDialog.setMessage(stringToHTML(message));
    }


    // Set an EditText view to get user input
    final EditText input = new EditText(activity);
    alertDialog.setView(input);
    // prevents the user from escaping the dialog by hitting the Back button
    alertDialog.setCancelable(false);
    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                HideKeyboard((View) input);
                AfterTextInput(input.getText().toString());
              }
            });

    //If cancelable, then add the CANCEL button
    if (cancelable)  {
      final String cancelButtonText = activity.getString(android.R.string.cancel);
      alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, cancelButtonText,
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  HideKeyboard((View) input);
                  //User pressed CANCEL. Raise AfterTextInput with CANCEL
                  AfterTextInput(cancelButtonText);
                }
              });
    }
    alertDialog.show();
  }

  private void textInputDialogMaterial(String message, String title, boolean cancelable) {

    SpannableString ss1=  new SpannableString(title);
    ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss1.length(), 0);

    SpannableString spannableString=null;
    if (linkify) {
      spannableString = new SpannableString(message);
      spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString.length(), 0);
      Linkify.addLinks(spannableString, Linkify.ALL);
    }


    final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();

    alertDialog.setTitle(ss1);

    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

    if (linkify) {
      alertDialog.setMessage(spannableString);
    } else {
      SpannableString sp = stringToHTML(message);
      sp.setSpan(new ForegroundColorSpan(Color.BLACK), 0, sp.length(), 0);
      alertDialog.setMessage(sp);
    }

    // Set an EditText view to get user input
    final EditText input = new EditText(activity);
    input.setTextColor(Color.BLACK);

    alertDialog.setView(input);
    // prevents the user from escaping the dialog by hitting the Back button
    alertDialog.setCancelable(false);
    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            HideKeyboard((View) input);
            AfterTextInput(input.getText().toString());
          }
        });

    //If cancelable, then add the CANCEL button
    if (cancelable)  {
      final String cancelButtonText = activity.getString(android.R.string.cancel);
      alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, cancelButtonText,
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              HideKeyboard((View) input);
              //User pressed CANCEL. Raise AfterTextInput with CANCEL
              AfterTextInput(cancelButtonText);
            }
          });
    }
    alertDialog.show();
  }

  /**
  * Hide soft keyboard after user either enters text or cancels.
  */
  public void HideKeyboard(View view) {
    if (view != null) {
      InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(),  0);
    }
  }

  /**
   * Event raised after the user has responded to ShowTextDialog.
   * @param response is the text that was entered
   */
  @SimpleEvent
  public void AfterTextInput(String response) {
    EventDispatcher.dispatchEvent(this, "AfterTextInput", response);
  }

  public void ShowAlert(final String notice) {
    ShowAlert(notice, true, 1);
  }
  /**
   * Display a temporary notification
   *
   * @param notice the text of the notification
   */
  @SimpleFunction(description = "Allows you display professional looking styled Alerts. Possible values are 1 through 6. " +
          "If showAtBottom is false, then alert will be displayed at center")
  public void ShowAlert(final String notice, final boolean showAtBottom, final int alertType) {
    handler.post(new Runnable() {
      public void run() {
        showStyledAlert(notice, showAtBottom, alertType);
      }
    });
  }


  public void showStyledAlert(final String notice, final boolean showAtBottom, final int alertType) {
    Toast toast;
    switch (alertType) {
      case 1:
        toast = StyledToast.makeText(form, notice, StyledToast.LENGTH_LONG, StyledToast.SUCCESS, false);
        if (!showAtBottom) toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
        toast.show();
        break;

      case 2:
        toast = StyledToast.makeText(form, notice, StyledToast.LENGTH_LONG, StyledToast.WARNING, false);
        if (!showAtBottom) toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
        toast.show();
        break;
      case 3:
        toast = StyledToast.makeText(form, notice, StyledToast.LENGTH_LONG, StyledToast.ERROR, false);
        if (!showAtBottom) toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
        toast.show();
        break;
      case 4:
        toast = StyledToast.makeText(form, notice, StyledToast.LENGTH_LONG, StyledToast.INFO, false);
        if (!showAtBottom) toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
        toast.show();
        break;
      case 5:
        toast = StyledToast.makeText(form, notice, StyledToast.LENGTH_LONG, StyledToast.DEFAULT, false);
        if (!showAtBottom) toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
        toast.show();
        break;
      case 6:
        toast = StyledToast.makeText(form, notice, StyledToast.LENGTH_LONG, StyledToast.CONFUSING, false);
        if (!showAtBottom) toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
        toast.show();
        break;
        default:
          // same as 1. todo: put 1 and this into a method call
          toast = StyledToast.makeText(form, notice, StyledToast.LENGTH_LONG, StyledToast.SUCCESS, false);
          if (!showAtBottom) toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
          toast.show();
    }
  }


  /**
   * Specifies the length of time that the alert is shown -- either "short" or "long".
   *
   * @param length  Length of time that an alert is visible
   */
  @DesignerProperty(
      editorType = PropertyTypeConstants.PROPERTY_TYPE_TOAST_LENGTH,
      defaultValue = Component.TOAST_LENGTH_LONG + "")
  @SimpleProperty(
      userVisible = false)
  public void NotifierLength(int length){
    notifierLength = length;
  }

  @SimpleProperty(
      description="specifies the length of time that the alert is shown -- either \"short\" or \"long\".",
      category = PropertyCategory.APPEARANCE)
  public int NotifierLength() {
    return notifierLength;
  }

  /**
   * Specifies the alert's background color.
   *
   * @param argb  background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
  @SimpleProperty(description="Specifies the background color for alerts (not dialogs).")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
  }

  /**
   * Returns the alert's text color.
   *
   * @return  text RGB color with alpha
   */
  @SimpleProperty(description = "Specifies the text color for alerts (not dialogs).",
      category = PropertyCategory.APPEARANCE)
  public int TextColor() {
    return textColor;
  }

  /**
   * Specifies the alert's text color.
   *
   * @param argb  text RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void TextColor(int argb) {
    textColor = argb;
  }

  // show a toast using a TextView, which allows us to set the
  // font size.  The default toast is too small.
  private void toastNow (String message) {
    // The notifier font size for more recent releases seems too
    // small compared to early releases.
    // This sets the fontsize according to SDK level,  There is almost certainly
    // a better way to do this, with display metrics for example, but
    // I (Hal) can't figure it out.
    int fontsize = (SdkLevel.getLevel() >= SdkLevel.LEVEL_ICE_CREAM_SANDWICH)
        ? 22 : 15;
    Toast toast = Toast.makeText(activity, message, notifierLength);
    toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
    TextView textView = new TextView(activity);
    textView.setBackgroundColor(backgroundColor);
    textView.setTextColor(textColor);
    textView.setTextSize(fontsize);
    Typeface typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    textView.setTypeface(typeface);
    textView.setPadding(10, 10, 10, 10);
    // Note: The space added to the message below is a patch to work around a bug where,
    // in a message with multiple words, the trailing words do not appear.  Whether this
    // bug happens depends on the version of the OS and the exact characters in the message.
    // The cause of the bug is that the textView parameter are not being set correctly -- I don't know
    // why not.   But as a result, Android will sometimes compute the length of the required
    // textbox as one or two pixels short.  Then, when the message is displayed, the
    // wordwrap mechanism pushes the rest of the words to a "next line" that does not
    // exist.  Adding the space ensures that the width allocated for the text will be adequate.
    textView.setText(message + " ");
    toast.setView(textView);
    toast.show();
  }

  /**
   * Log an error message.
   *
   * @param message the error message
   */
  @SimpleFunction(description = "Writes an error message to the Android system log. " +
     "See the Google Android documentation for how to access the log.")
  public void LogError(String message) {
    Log.e(LOG_TAG, message);
  }

  /**
   * Log a warning message.
   *
   * @param message the warning message
   */
  @SimpleFunction(description = "Writes a warning message to the Android log. " +
     "See the Google Android documentation for how to access the log.")
  public void LogWarning(String message) {
    Log.w(LOG_TAG, message);
  }

  /**
   * Log an information message.
   *
   * @param message the information message
   */
  @SimpleFunction(description = "Writes an information message to the Android log.")
  public void LogInfo(String message) {
    Log.i(LOG_TAG, message);
  }
}
