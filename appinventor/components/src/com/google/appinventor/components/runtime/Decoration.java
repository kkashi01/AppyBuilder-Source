// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
package com.google.appinventor.components.runtime;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.LinearLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;


@DesignerComponent(version = YaVersion.DECORATION_COMPONENT_VERSION,
        description = "Decoration is a non-visible component that update odemessage xxxxxxx",
        category = ComponentCategory.SCREEN,
        nonVisible = true,
        iconName = "images/decoration.png")

@SimpleObject
public class Decoration extends AndroidNonvisibleComponent
        implements Component {

    private Context context;  // this was a local in constructor and final not private

    private ComponentContainer container;

    /**
     * Creates instance of new component.
     *
     * @param container the Form that this component is contained in.
     */
    public Decoration(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();

    }

    @SimpleFunction(description = "Margin is a way for a component to enforce some distance from others components. " +
            "By specifying margin for a component, we say that keep this much distance from this component.")
    public void SetMargin(AndroidViewComponent component, String values) {
        values = values.trim();
        if (values.isEmpty()) return;
        String sizes[] = values.split(",");
        List<Integer> actualSizes = new ArrayList<Integer>();
        for (int i = 0; i < sizes.length; i++) {
            int size = form.convertDpToDensity(Integer.valueOf(sizes[i].trim()));
            actualSizes.add(size);
        }

        try {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) component.getView().getLayoutParams();
            // left, top, bottom, right
            if (sizes.length == 1) {
                layoutParams.setMargins(actualSizes.get(1 - 1), actualSizes.get(1 - 1), actualSizes.get(1 - 1), actualSizes.get(1 - 1));
                component.getView().setLayoutParams(layoutParams);
            } else if (sizes.length == 2) {
                layoutParams.setMargins(actualSizes.get(2 - 1), actualSizes.get(1 - 1), actualSizes.get(2 - 1), actualSizes.get(1 - 1));
                component.getView().setLayoutParams(layoutParams);
            } else if (sizes.length == 4) {
                // we are allowing user to specify as t,l,b,r. However, setPadding order is different: l,t,b,r
                layoutParams.setMargins(actualSizes.get(2 - 1), actualSizes.get(1 - 1), actualSizes.get(4 - 1), actualSizes.get(3 - 1));
                component.getView().setLayoutParams(layoutParams);
            }
        } catch (Exception e) {
            form.dispatchErrorOccurredEvent(this, "SetMargin", ErrorMessages.ERROR_INVALID_TYPE, e.getMessage());
        }

    }
    @SimpleFunction(description = "The padding around the component. Padding is the space inside the border, between the border and the actual component content. " +
            "Use single number like 5 to specify padding for top, left, bottom, righ. " +
            "You can also use 4 different numbers like 5,0,10,0 for top, left, bottom right.")
    public void SetPadding(AndroidViewComponent component, String values) {
        values = values.trim();
        if (values.isEmpty()) return;
        String sizes[] = values.split(",");
        List<Integer> actualSizes = new ArrayList<Integer>();
        for (int i = 0; i < sizes.length; i++) {
            int size = form.convertDpToDensity(Integer.valueOf(sizes[i].trim()));
            actualSizes.add(size);
        }

        // left, top, bottom, right
        try {
            if (sizes.length == 1) {
                component.getView().setPadding(actualSizes.get(1 - 1), actualSizes.get(1 - 1), actualSizes.get(1 - 1), actualSizes.get(1 - 1));
            } else if (sizes.length == 2) {
                component.getView().setPadding(actualSizes.get(2 - 1), actualSizes.get(1 - 1), actualSizes.get(2 - 1), actualSizes.get(1 - 1));
            } else if (sizes.length == 4) {
                // we are allowing user to specify as t,l,b,r. However, setPadding order is different: l,t,b,r
                component.getView().setPadding(actualSizes.get(2 - 1), actualSizes.get(1 - 1), actualSizes.get(4 - 1), actualSizes.get(3 - 1));
            }
        } catch (Exception e) {
            form.dispatchErrorOccurredEvent(this, "SetPadding", ErrorMessages.ERROR_INVALID_TYPE, e.getMessage());
        }
    }

    @SimpleFunction(description = "This block allows you to create a rectangle or round shape for the visible component. " +
            "You can use Color for backgroundColor and borderColor. ")
    public void SetShape(AndroidViewComponent component, int backgroundColor, int borderColor, boolean isRound) {
        try {
            ViewUtil.setShape(component.getView(), backgroundColor, borderColor, isRound);
        } catch (Exception e) {
            form.dispatchErrorOccurredEvent(this, "SetShape", ErrorMessages.ERROR_INVALID_TYPE, e.getMessage());
        }

    }

    //has coding error. Let's just forget it for now

    // https://gist.github.com/ptsiogas/e11b29a4324a58cb4d41
   // @SimpleFunction(description = "Adds a Gradient color to a visible component. Given T (for Top), B (for Bottom), L (for Left), R (for right), you can use values for orientation: T_B, B_T, L_R, R_L, BL_TR, BR_TL, TL_BR, TR_BL")
    public void SetGradient(AndroidViewComponent component, int startColor, int endColor, String orientation) {
       LayerDrawable layerdDrawable = setLayerShadow(startColor, endColor, orientation);
       component.getView().setBackground(layerdDrawable);
    }

    /**
     * Creates background color with shadow effect - programmatically
     *
     * @param color    the background color
     */
    private LayerDrawable setLayerShadow(int rgbStart, int rgbEnd, String orientation) {
        orientation = orientation.trim().toUpperCase();

        GradientDrawable shadow;
        int strokeValue = 6;
        int radiousValue = 2;
        try{
        int[] colorList = {rgbStart, rgbEnd};
            shadow = new GradientDrawable(getGradOrientation(orientation), colorList);
            shadow.setCornerRadius(radiousValue);
        }
        catch(Exception e){
            int[] colors1 = {Color.parseColor("#419ED9"), Color.parseColor("#419ED9")};
            shadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors1);
            shadow.setCornerRadius(radiousValue);
            e.printStackTrace();
        }


        int[] colors = {Color.parseColor("#D8D8D8"), Color.parseColor("#E5E3E4")};

        GradientDrawable backColor = new GradientDrawable(GradientDrawable.Orientation.BL_TR, colors);
        backColor.setCornerRadius(radiousValue);
        backColor.setStroke(strokeValue, Color.parseColor("#D8D8D8"));

        //finally c.reate a layer list and set them as background.
        Drawable[] layers = new Drawable[2];
        layers[0] = backColor;
        layers[1] = shadow;

        LayerDrawable layerList = new LayerDrawable(layers);
        layerList.setLayerInset(0, 0, 0, 0, 0);
        layerList.setLayerInset(1, strokeValue, strokeValue, strokeValue, strokeValue);
        return layerList;
    }

    private GradientDrawable.Orientation getGradOrientation(String orientation){

        GradientDrawable.Orientation result;

        switch(orientation) {

            case "BL_TR":
                //draw the gradient from the bottom-left to the top-right
                result = GradientDrawable.Orientation.BL_TR;
                break;

            case "B_T":
                //draw the gradient from the bottom to the top
                result = GradientDrawable.Orientation.BOTTOM_TOP;
                break;

            case "BR_TL":
                //draw the gradient from the bottom-right to the top-left
                result = GradientDrawable.Orientation.BR_TL;
                break;

            case "L_R":
                //draw the gradient from the left to the right
                result = GradientDrawable.Orientation.LEFT_RIGHT;
                break;

            case "RL":
                //draw the gradient from the right to the left
                result = GradientDrawable.Orientation.RIGHT_LEFT;
                break;

            case "TL_BR":
                //draw the gradient from the top-left to the bottom-right
                result = GradientDrawable.Orientation.TL_BR;
                break;

            case "T_B":
                //draw the gradient from the top to the bottom
                result = GradientDrawable.Orientation.TOP_BOTTOM;
                break;

            case "TR_BL":
                //draw the gradient from the top-right to the bottom-left
                result = GradientDrawable.Orientation.TR_BL;
                break;

            default:
                //draw the gradient from the left to the right
                result = GradientDrawable.Orientation.LEFT_RIGHT;
                break;
        }

        return result;
    }
}
