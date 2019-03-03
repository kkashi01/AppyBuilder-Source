// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.TextViewUtil;

/**
 * This class is used to display a ToggleButton.
 * <p>A ToggleButton Displays checked/unchecked states as a button with a 'light' indicator and by default
 * accompanied with the text 'ON' or 'OFF'.
 *
 */
@DesignerComponent(version = YaVersion.TOGGLER_COMPONENT_VERSION,
    description = "Update ODE ",
    category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class Switch extends AndroidViewComponent implements CompoundButton.OnCheckedChangeListener{
  private final static String LOG_TAG = "Switch";
    private final Activity context;
  private android.widget.Switch view;
    private int textColor;
    private String switchText = "Switch: ";
    private int trackColorLeftChecked = Color.parseColor("#673AB7");
    private int trackColorRightNotChecked = Color.parseColor("#B39DDB");
    private int thumbColorChecked = Color.parseColor("#673AB7");
    private int thumbColorNotChecked = Color.parseColor("#B39DDB");

    /**
   * Creates a new Switch component.
   *
   * @param container container that the component will be placed in
   */
  public Switch(ComponentContainer container) {
      super(container);
      view = new android.widget.Switch(container.$context());
      container.$add(this);
      this.context = container.$context();

      view.setOnCheckedChangeListener(this);
      view.setChecked(true);
      Text(switchText);
      FontSize(Component.FONT_DEFAULT_SIZE);
      TextColor(Component.COLOR_BLACK);
//      ThumbColor(Component.COLOR_RED);
//      TrackColor(Component.COLOR_LTGRAY);
      ThumbColorChecked(Component.COLOR_RED);
      TrackColorChecked(Component.COLOR_GREEN);
      ThumbColorUnchecked(Component.COLOR_LTGRAY);
      TrackColorUnchecked(Component.COLOR_LTGRAY);
  }

  @Override
  public View getView() {
    return view;
  }


    /**
     * The text for the button when it is checked
     * @param text  The text for the button when it is checked
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Switch: ")
    @SimpleProperty(description = "Text for switch")
    public void Text(String text) {
        switchText = text;
        view.setText(text);
    }

    @SimpleProperty(description = "Gets the value of Switch text")
    public String Text() {
        return switchText;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty
    public void TextColor(int argb) {
        textColor = argb;
        if (argb != Component.COLOR_DEFAULT) {
            TextViewUtil.setTextColor(view, argb);
        } else {
            TextViewUtil.setTextColor(view, Component.COLOR_BLACK);
        }
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public int TextColor() {
        return textColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
    @SimpleProperty(description = "Sets up the font size for the component")
    public void FontSize(float size) {
        if (view == null) {
            view = new android.widget.Switch(container.$context());
        }
        view.setTextSize(size);
        view.invalidate();
//        TextViewUtil.setFontSize(view, size);
    }


    /**
     * enables or disables the component
     *
     * @param enabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Enables or disables the component")
    public void Enabled(boolean enabled) {
       view.setEnabled(enabled);
    }

//    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_RED)
    @SimpleProperty(description = "The thumb color. Works only for Android +4.1.x", userVisible = false)
    public void ThumbColor(int color) {
        if (android.os.Build.VERSION.SDK_INT < SdkLevel.LEVEL_JELLYBEAN) {
            return;
        }
        try {
            //        int tint = Color.parseColor(String.valueOf(argb));  //don't do this, crashes
            // specifically on Samsung galaxy 4.2, the drawable returns null
            Drawable drawable =  view.getThumbDrawable();
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        } catch (Exception e) {
            //no-op
        }
    }

//     @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
    @SimpleProperty(description = "The track color. Works only for Android +4.1.x", userVisible = false)
    public void TrackColor(int argb) {
        if (android.os.Build.VERSION.SDK_INT < SdkLevel.LEVEL_JELLYBEAN) {
            return;
        }
         try {
             Drawable drawable =  view.getTrackDrawable();
//         drawable.setColorFilter(argb, PorterDuff.Mode.MULTIPLY);   //thumb color won't change between on, off state
             drawable.setColorFilter(argb, PorterDuff.Mode.LIGHTEN);
//        int tint = Color.parseColor(String.valueOf(argb));
//        view.getTrackDrawable().setColorFilter(tint, PorterDuff.Mode.MULTIPLY);
         } catch (Exception e) {
             //no-op
         }
     }

    /**
     * Returns true if the toggler is on.
     *
     * @return  {@code true} indicates toggler is in ON state, {@code false} OFF state
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean Checked() {
        return view.isChecked();
    }

    /**
     * Specifies whether the toggler should be in ON state or OFF state
     *
     * @param enabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Sets state to checked or unchecked")
    public void Checked(boolean enabled) {
       view.setChecked(enabled);
    }

    /**
     * Default Changed event handler.
     */
    @SimpleEvent(description = "Triggered when state of Switch changes. Use isChecked to determine if checked or not-checked")
    public void Click(boolean isChecked) {
        EventDispatcher.dispatchEvent(this, "Click", isChecked);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
        view.setChecked(enabled);
        if (android.os.Build.VERSION.SDK_INT >= SdkLevel.LEVEL_JELLYBEAN) {
            // Now dealing with API +16. We can change color
        Drawable thumbDrawable = view.getThumbDrawable();
        Drawable trackDrawable = view.getTrackDrawable();
        if (view.isChecked()) {
            trackDrawable.setColorFilter(trackColorLeftChecked, PorterDuff.Mode.MULTIPLY);
            thumbDrawable.setColorFilter(thumbColorChecked, PorterDuff.Mode.MULTIPLY);
        } else {
            trackDrawable.setColorFilter(trackColorRightNotChecked, PorterDuff.Mode.MULTIPLY);
            thumbDrawable.setColorFilter(thumbColorNotChecked, PorterDuff.Mode.MULTIPLY);
        }
        }
        Click(enabled);
    }


    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
    @SimpleProperty(description = "Sets up the right track color - when switch is NOT checked. This works only for Android Version +4.1.x")
    public void TrackColorUnchecked(int color) {
        if (android.os.Build.VERSION.SDK_INT >= SdkLevel.LEVEL_JELLYBEAN) {
        this.trackColorRightNotChecked = color;
        if (!view.isChecked()) {
            Drawable drawable =  view.getTrackDrawable();
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_GREEN)
    @SimpleProperty(description = "Sets up the left track color - when switch is checked. This works only for Android Version +4.1.x")
    public void TrackColorChecked(int color) {
        if (android.os.Build.VERSION.SDK_INT >= SdkLevel.LEVEL_JELLYBEAN) {
        this.trackColorLeftChecked = color;
        if (view.isChecked()) {
            Drawable drawable =  view.getTrackDrawable();
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_RED)
    @SimpleProperty(description = "Sets up the Switch Thumb color, when checked. This works only for Android Version +4.1.x")
    public void ThumbColorChecked(int color) {
        if (android.os.Build.VERSION.SDK_INT >= SdkLevel.LEVEL_JELLYBEAN) {
        this.thumbColorChecked = color;
        if(view.isChecked()){
            Drawable drawable =  view.getThumbDrawable();
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_GRAY)
    @SimpleProperty(description = "Sets up the Switch Thumb color, when checked. This works only for Android Version +4.1.x")
    public void ThumbColorUnchecked(int color) {
        if (android.os.Build.VERSION.SDK_INT >= SdkLevel.LEVEL_JELLYBEAN) {
        this.thumbColorNotChecked = color;
        if(!view.isChecked()){
            Drawable drawable =  view.getThumbDrawable();
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }
}
