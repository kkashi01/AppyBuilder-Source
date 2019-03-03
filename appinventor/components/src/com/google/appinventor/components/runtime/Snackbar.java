// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.DeviceDimensionsUtil;


/**
 * Snackbar component
 */
@DesignerComponent(version = YaVersion.SNACKBAR_COMPONENT_VERSION,
        description = "Snackbar is a non-visible component update OdeMessage xxxxxxxxxxxxxx",
        category = ComponentCategory.SCREEN,
        nonVisible = true,
        iconName = "images/snackbar.png")

@SimpleObject
@UsesAssets(fileNames = "fontawesome-webfont.ttf")
public class Snackbar extends AndroidNonvisibleComponent implements Component {

    private static final String LOG_TAG = "Snackbar";
    private final Activity activity;

    private float textSize;
    private int textColor;
    private int backgroundColor;
    private Context context;  // this was a local in constructor and final not private
    private RelativeLayout relative;
    private ComponentContainer container;
    private boolean bold = true;
    private boolean center;
    private int maxY;
    private int maxX;
    private int centerY;
    private float snackbarHeight;
    private LinearLayout snackLayout;
    private TextView snackMessage;

    public Snackbar(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();
        activity = (Activity) context;

        TextSize(16);
        BackgroundColor(Component.COLOR_DKGRAY);
        TextColor(Component.COLOR_WHITE);
        FontBold(true);
    }

    @SimpleFunction(description = "Creates a Snackbar that can be displayed at bottom or center")
    public void Create(String message, final String action1, final String action2) {

        final Typeface typeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(), "component/fontawesome-webfont.ttf");
        int style = 0;
        if (bold) {
            style |= Typeface.BOLD;
        }

        Point p = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(p);
        maxY = p.y;
        maxX = p.x;
        centerY = maxY / 2;

        if (snackLayout != null)
            snackLayout.setVisibility(View.GONE);

        float density = (int) DeviceDimensionsUtil.getDensity(form);
        snackbarHeight = DeviceDimensionsUtil.convertDpToPixel(48, form);

        // create the layoutparam
        LinearLayout.LayoutParams lpSnack = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) snackbarHeight);

        // create the layout
        snackLayout = new LinearLayout(form);
        snackLayout.setOrientation(LinearLayout.HORIZONTAL);
        snackLayout.setLayoutParams(lpSnack);
        snackLayout.setGravity(17);
        snackLayout.setBackgroundColor(backgroundColor);

        if (Build.VERSION.SDK_INT >= 21)
            snackLayout.setElevation(10.0F);
        int i = (int) (density * 24);

        // add the message
        snackMessage = new TextView(form);
        snackMessage.setText(message);
        snackMessage.setTextSize(textSize);
        snackMessage.setTextColor(textColor);
        snackMessage.setGravity(19);
        snackMessage.setTypeface(typeface, style);

        snackMessage.setPadding(i, 0, i, 0);
//        TextViewUtil.setFontTypeface(snackMessage, Component.TYPEFACE_DEFAULT, bold, false);

        // Create a table layout so that we can align message/action horizontally
        android.widget.TableLayout.LayoutParams lpTable = new TableLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0F);

        // create message and add as 1st cell into table
        snackMessage.setLayoutParams(lpTable);
        snackLayout.addView(snackMessage);

        // ========== 1st action button
        if (!action1.trim().isEmpty()) {
            // create 1st action button and add into next cell
            TextView snackButton = new TextView(form);
            snackButton.setText(action1);
            snackButton.setTextSize(textSize);
//            snackButton.setTextColor(Color.parseColor(colorFontButton));
            snackButton.setTextColor(textColor);
            snackButton.setTypeface(typeface, style);

            snackButton.setGravity(21);
            snackButton.setPadding(i, 0, i, 0);
            snackLayout.addView(snackButton);

            // set the button listner
            snackButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View paramView) {
                    AfterAction(action1);
                    Close();
                }
            });
        }

        // =========== action 2
        if (!action2.trim().isEmpty()) {
            // create 1st action button and add into next cell
            TextView snackButton = new TextView(form);
            snackButton.setText(action2);
            snackButton.setTextSize(textSize);
            snackButton.setTextColor(textColor);
            snackButton.setTypeface(typeface, style);
            snackButton.setGravity(21);
            snackButton.setPadding(i, 0, i, 0);
            snackLayout.addView(snackButton);

            // set the button listner
            snackButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View paramView) {
                    AfterAction(action2);
                    Close();
                }
            });
        }

        relative = new RelativeLayout(form);
        relative.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        relative.setBackgroundColor(0);
        relative.addView(snackLayout);
        RelativeLayout.LayoutParams lpRelative = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        lpRelative.addRule(12);

        activity.addContentView(relative, lpRelative);

        relative.setGravity(Gravity.BOTTOM);
    }


    @SimpleFunction(description = "Reports if Snackbar is open or not")
    public boolean IsOpen() {
        return snackLayout.getVisibility() == View.VISIBLE;
    }

    @SimpleFunction(description = "If component has already been created, it will close it")
    public void Close() {
        if (snackLayout == null) return;

        snackLayout.animate().translationY(snackLayout.getHeight()).alpha(0.9F).setDuration(300L).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                snackLayout.setVisibility(View.GONE);
            }
        });

    }

    @SimpleProperty(description = "Sets the background color.", category = PropertyCategory.APPEARANCE)
    public int BackgroundColor() {
        return backgroundColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
    @SimpleProperty(description = "Sets the background property of this component")
    public void BackgroundColor(int argb) {
        backgroundColor = argb;
    }

    @SimpleProperty(description = "The text color of the items.", category = PropertyCategory.APPEARANCE)
    public int TextColor() {
        return textColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
    @SimpleProperty
    public void TextColor(int argb) {
        textColor = argb;
    }

    @SimpleProperty(description = "The font size items", category = PropertyCategory.APPEARANCE)
    public float TextSize() {
        return this.textSize;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = "16")
    @SimpleProperty
    public void TextSize(float size) {
        this.textSize = size;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, description = "If set, button text is displayed in bold.")
    public boolean FontBold() {
        return bold;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void FontBold(boolean bold) {
        this.bold = bold;
    }

    @SimpleEvent(description = "Triggered after Snackbar action has been clicked")
    public void AfterAction(String action) {
        EventDispatcher.dispatchEvent(Snackbar.this, "AfterAction", action);
    }
}
