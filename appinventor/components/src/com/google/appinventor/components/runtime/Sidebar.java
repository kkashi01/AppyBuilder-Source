// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

package com.google.appinventor.components.runtime;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Sidebar component
 */
@DesignerComponent(version = YaVersion.SIDEBAR_COMPONENT_VERSION,
        description = "Sidebar is is a non-visible component update OdeMessage xxxxxxxxxxxxxx",
        category = ComponentCategory.SCREEN,
        nonVisible = true,
        iconName = "images/sidebar.png")

@SimpleObject
@UsesAssets(fileNames = "fontawesome-webfont.ttf,onepixtransp.png")
public class Sidebar extends AndroidNonvisibleComponent implements Component, OnOptionsItemSelectedListener {

    private static final String LOG_TAG = "Sidebar";
    private final Activity activity;
    private int selectionIndex = -1;

    private float textSize;
    private int textColor;
    private int backgroundColor;

    private Context context;  // this was a local in constructor and final not private
    private double maxNavWidth = 999999; // DeviceDimensionsUtil.getDisplayWidth(this); // set up the initial default width

    private ComponentContainer container;
    private LinearLayout drawerLayout;
    private LinearLayout drawerPane;
//    private android.app.ActionBar actionBar;
    private List<String> iconsArrayList = new ArrayList<>();
    private List<String> itemsArrayList = new ArrayList<>();
    private boolean fontBold = false;

    /**
     * Creates a new TinyDB component.
     *
     * @param container the Form that this component is contained in.
     */
    public Sidebar(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();
        activity = (Activity) context;

        TextSize(16);
        TextColor(Component.COLOR_BLACK);
        BackgroundColor(Component.COLOR_WHITE);

    }

    @SimpleFunction(description = "Creates a Sidebar menu. It is a panel that displays the app’s main options " +
            "on the left edge of the screen. It is hidden most of the time, but is revealed when the user swipes " +
            "a finger from the left edge of the screen. Add headerImagePath from asset folder or point to Internet or leave empty." +
            "For listItems, provide a CSV of items and for listIcons, leave empty or provide a CSV of icons " +
            "from here: https://fontawesome.com/cheatsheet  e.g. f064 for share icon")
    public void Create(String listItems, String listIcons, String headerImagePath) {
        // if already opened, make sure you close it first
        Close();

        headerImagePath = headerImagePath.trim();
//        final String finalPicturePath = headerImagePath.trim();

        Display screenOrientation = activity.getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;

        double percent;
        android.widget.ListView listView = new android.widget.ListView(container.$form());
        // if portrait, then use 75 percent of screen, then 45%
        if (DeviceDimensionsUtil.isPortrait(form)) percent = .75;
        else percent = .45;


        Log.d(LOG_TAG, "xxxx percent is:" + percent);
        maxNavWidth = (DeviceDimensionsUtil.getDisplayWidth(form) * percent);

        boolean hasHeader = false;
        if (!headerImagePath.isEmpty()) {
            try {
                BitmapDrawable drawable = MediaUtil.getBitmapDrawable(container.$form(), headerImagePath);

                Bitmap bitmap1 = drawable.getBitmap();

                Bitmap bitmap = ShapeUtil.scaleDown(bitmap1, (int) maxNavWidth, false);
                drawable = new BitmapDrawable(context.getResources(), bitmap);
                ImageView imageView = new ImageView(container.$form());
                imageView.setImageDrawable(drawable);
                listView.addHeaderView(imageView);
                hasHeader = true;
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error loading navigation image");
            }
        }

        final boolean finalHasHeader = hasHeader;

        drawerLayout = new LinearLayout(form);

        final LinearLayout.LayoutParams drawerLayout_params = new LinearLayout.LayoutParams((int) maxNavWidth, LinearLayout.LayoutParams.MATCH_PARENT);

        drawerLayout.setId(0);
        drawerPane = new LinearLayout(form);

        listView.setOnTouchListener(new OnSwipeTouchListener(form) {
            public void onSwipeTop() {
//                Toast.makeText(LoginActivity5.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
                // not needed
            }

            public void onSwipeLeft() {
                // needed
                Close();
            }

            public void onSwipeBottom() {
            }

        });


        drawerPane.setGravity(Gravity.START);
        drawerPane.setClickable(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        drawerPane.setLayoutParams(drawerLayout_params);

        drawerPane.addView(listView, drawerLayout_params);

        drawerPane.setOrientation(LinearLayout.VERTICAL);
        drawerPane.setBackgroundColor(backgroundColor);

        activity.addContentView(drawerLayout, drawerLayout_params);

        drawerLayout.addView(drawerPane, drawerLayout_params);

        itemsArrayList = new ArrayList<>();
        iconsArrayList = new ArrayList<>();

        YailList items = ElementsUtil.elementsFromString(listItems);
        YailList icons = ElementsUtil.elementsFromString(listIcons);

        if (items.size() != 0)
            Collections.addAll(itemsArrayList, items.toStringArray());

        if (icons.size() != 0)
            Collections.addAll(iconsArrayList, icons.toStringArray());

        for (int i=0; i <iconsArrayList.size(); i++) {
            iconsArrayList.set(i, new String(Character.toChars(Integer.parseInt(iconsArrayList.get(i), 16))));
        }

        List<String> origList = new ArrayList<String>(itemsArrayList);

        origList = new LinkedList<String>(itemsArrayList);

        // https://fontawesome.com/cheatsheet

        for (int i = 0; i < itemsArrayList.size(); i++) {
            try {
                if (iconsArrayList.size() - 1 >= i && !iconsArrayList.get(i).isEmpty()) {
                    itemsArrayList.set(i, iconsArrayList.get(i) + "\t\t" + itemsArrayList.get(i));
                }
            } catch (Exception e) {
                itemsArrayList.set(i, itemsArrayList.get(i));
            }
        }

        // ListView Item Click Listener
        final List<String> finalOrigList = origList;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // since list has icons, we use the origList to get the item

                if (!finalHasHeader) {
                    AfterSelecting(position+1, (String) finalOrigList.get(position));  // 1-based
                } else {
                    // has header
                    if (position == 0) {
                        AfterSelecting(position, "NAV_HEADER");
                    } else {
                        AfterSelecting(position, (String) finalOrigList.get(position - 1));
                    }
                }

                Close();
            }
        });

        setAdapterData(listView);

        listView.setDivider(new ColorDrawable(Color.argb(55, 0, 0, 0))); // Set ListView divider color
        listView.setDividerHeight(2);  // set ListView divider height

        addSwipeToMain();

        // hide drawer away
        int width = DeviceDimensionsUtil.getDisplayWidth(form);
        drawerLayout.setX(-width);

    }

    private void setAdapterData(ListView listView) {

        if (listView == null) return;
        ViewUtil.setShape(listView, backgroundColor, Component.COLOR_LTGRAY, true);

        final Typeface fonts = Typeface.createFromAsset(context.getApplicationContext().getAssets(), "component/fontawesome-webfont.ttf");
        listView.setDivider(new ColorDrawable(Color.argb(55, 0, 0, 0))); // Set ListView divider color
        listView.setDividerHeight(2);  // set ListView divider height
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(form, android.R.layout.simple_list_item_1, itemsArrayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                textView.setTextColor(textColor);
                textView.setTypeface(fonts);
                textView.setTextSize(textSize);
                textView.setTypeface(textView.getTypeface(), fontBold? Typeface.BOLD : Typeface.NORMAL);
                return view;
            }
        };

        listView.setAdapter(adapter);
    }

    private void addSwipeToMain() {
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) (activity.findViewById(android.R.id.content))).getChildAt(0);
        viewGroup.setOnTouchListener(new OnSwipeTouchListener(form) {
            public void onSwipeTop() {
//                Toast.makeText(LoginActivity5.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
//                Toast.makeText(LoginActivity5.this, "right", Toast.LENGTH_SHORT).show();
                Open();
            }

            public void onSwipeLeft() {
                Close();
            }

            public void onSwipeBottom() {
            }

        });
    }

    @SimpleFunction(description = "If Sidebar has already been created, then it will open it")
    public void Open() {
        if (drawerLayout == null) return;

        if (IsOpen()) return;

        ObjectAnimator.ofFloat(drawerLayout, "x", -drawerLayout.getWidth(), 0)
                .setDuration(500)
                .start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(form, "options menu selected", Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(form, "Drawer: " + (drawerLayout.getX() >= 0), Toast.LENGTH_SHORT).show();
                if (drawerLayout.getX() >= 0) Close();
                else Open();
                break;
        }

        return true;
    }

    public void navIconClickedOpenClose() {
//        Toast.makeText(form, "navIcon is getting invoked", Toast.LENGTH_SHORT).show();
        if (drawerLayout == null) return;

        if (drawerLayout.getX() >= 0) Close();
        else Open();
    }

    @SimpleFunction(description = "If Sidebar has already been created, it will close it")
    public void Close() {
        if (drawerLayout == null) return;

        if (!IsOpen()) return;

        ObjectAnimator.ofFloat(drawerLayout, "x", 0, -drawerLayout.getWidth())
                .setDuration(500)
                .start();

    }

    @SimpleFunction(description = "Reports if Sidebar is open or not")
    public boolean IsOpen() {
        boolean isOpen = this.drawerLayout.getX() >= 0;
//        Toast.makeText(form, isOpen + "/x is:" + this.drawerLayout.getX(), Toast.LENGTH_SHORT).show();

        return isOpen;
    }

    @SimpleEvent(description = "Triggered when user picks (selects) an item from Sidebar")
    public void AfterSelecting(int position, String itemValue) {
        selectionIndex = position;
        EventDispatcher.dispatchEvent(this, "AfterSelecting", position, itemValue);
    }

    @SimpleProperty(description = "Sets the background color.", category = PropertyCategory.APPEARANCE)
    public int BackgroundColor() {
        return backgroundColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
    @SimpleProperty
    public void BackgroundColor(int argb) {
        backgroundColor = argb;
    }

    @SimpleProperty(description = "The text color of the items.", category = PropertyCategory.APPEARANCE)
    public int TextColor() {
        return textColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty
    public void TextColor(int argb) {
        textColor = argb;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public boolean FontBold() {
        return fontBold;
    }

    /**
     * Specifies whether the label's text should be bold.
     * Some fonts do not support bold.
     *
     * @param bold  {@code true} indicates bold, {@code false} normal
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "If true, then Sidebar fonts will be bold")
    public void FontBold(boolean bold) {
        this.fontBold = bold;
    }

    @SimpleProperty(description = "The font size items", category = PropertyCategory.APPEARANCE)
    public float TextSize() {
        return this.textSize;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = 16 + "")
    @SimpleProperty
    public void TextSize(float size) {
        this.textSize = size;
    }


    @SimpleProperty(
            description = "The index of the currently selected item, starting at " +
                    "1.  If no item is selected, the value will be -1.  ",
            category = PropertyCategory.BEHAVIOR)
    public int SelectionIndex() {
        return selectionIndex;
    }
}
