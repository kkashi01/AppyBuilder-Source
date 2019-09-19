// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.*;
import android.widget.LinearLayout;
import com.firebase.client.collection.LLRBNode;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.YaVersion;

import java.io.IOException;
import java.io.InputStream;

/**
 * A vertical arrangement of components
 * @author sharon@google.com (Sharon Perl)
 *
 */

@DesignerComponent(version = YaVersion.VERTICALARRANGEMENT_COMPONENT_VERSION,
    description = "<p>A formatting element in which to place components " +
    "that should be displayed one below another.  (The first child component " +
    "is stored on top, the second beneath it, etc.)  If you wish to have " +
    "components displayed next to one another, use " +
    "<code>HorizontalArrangement</code> instead.</p>",
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class VerticalArrangement extends HVArrangement {

  public VerticalArrangement(ComponentContainer container) {
    super(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
      ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
  }

  public static Bitmap getBitmapFromAsset(Context context, String filePath) {
    AssetManager assetManager = context.getAssets();

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
//  @SimpleFunction(description = "Creates a Material Card. You can plug-in empty Text block for each of the parameters")
//  public void CreateCard(String topImagePath, String title, String subTitle, String content) {
//    frameContainer.removeAllViews();
//
//    IsCard(true);
//
//    android.widget.LinearLayout linearLayout = new android.widget.LinearLayout($context());
//    linearLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
//    android.widget.LinearLayout.LayoutParams param = new android.widget.LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
//    linearLayout.setLayoutParams(param);
//    title=title.trim();
//    subTitle=subTitle.trim();
//    content=content.trim();
//
//    if (!(topImagePath.isEmpty())) {
//      ImageView ivLogo = new ImageView($context());
//      Bitmap myBitmap;
//      try {
//        java.io.File imgFile = new java.io.File(topImagePath);
//        if (imgFile.exists()) {
//          myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//        } else {
//          if ($form() instanceof ReplForm) {
//            java.io.File imgFile2 = new java.io.File("/mnt/sdcard/AppInventor/assets/" + topImagePath);
//            myBitmap = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
//          } else {
//            myBitmap = getBitmapFromAsset($context(), topImagePath);
//          }
//          linearLayout.addView(ivLogo);
//        }
//        ivLogo.setImageBitmap(myBitmap);
//      } catch (Exception ex) {
//        // no op
//      }
//    }
//
//    if (!title.equals("")) {
//      Log.d("VerticalArrangement", "adding title");
//      TextView tv = new TextView($context());
//      tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//      tv.setTextColor(Color.BLACK);
//      tv.setAlpha(0.9f);
//      tv.setTextSize(24);
//      linearLayout.addView(tv);
//    }
//
//    if (!subTitle.equals("")) {
//      Log.d("VerticalArrangement", "adding sub title");
//      TextView tv = new TextView($context());
//      tv.setTextColor(Color.BLACK);
//      tv.setTextSize(14);
//      tv.setAlpha(0.5f);
//      linearLayout.addView(tv);
//    }
//    if (!content.equals("")) {
//      Log.d("VerticalArrangement", "adding content");
//      TextView tv = new TextView($context());
//      tv.setTextColor(Color.BLACK);
//      tv.setTextSize(14);
//      linearLayout.addView(tv);
//    }
//
//
//    frameContainer.addView(linearLayout);
//  }


}
