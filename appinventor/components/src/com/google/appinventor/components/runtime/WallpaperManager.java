// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.squareup.picasso.Picasso;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Contains various helper methods
 *
 */
@DesignerComponent(version = YaVersion.WALLPAPER_COMPONENT_VERSION,
        description = "Non-visible blah blah blah",
        category = ComponentCategory.MEDIA,
        nonVisible = true,
        iconName = "images/wallpaper.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.SET_WALLPAPER,android.permission.WRITE_EXTERNAL_STORAGE" +
        ",android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "picasso-2.5.2.jar")
public class WallpaperManager extends AndroidNonvisibleComponent implements Component {
    private static final String LOG_TAG = "WallpaperManager";
    private final ComponentContainer container;
    private final Context context;
    private Activity activity;
    //private static final String TAG = KitchenSink.class.getCanonicalName();
    //private static final String processId = Integer.toString(android.os.Process.myPid());
    /**
     * Creates a new KitchenSink component.
     *
     * @param container the Form that this component is contained in.
     */
    public WallpaperManager(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();
        this.activity = container.$context();
    }

    /**
     * Sets the wallpaper
     */
    @SimpleFunction(description = "Creates a wallpaper from the given image path OR use image on Internet")
    public void SetWallpaper(String path) {
        if (path==null || path.isEmpty()) return;
        path=path.trim();
        final String finalPath = path;

        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncSetWallpaper(finalPath);
            }
        });

        if (true) return;
        Drawable drawable;
        try {
            android.app.WallpaperManager myWallpaperManager = android.app.WallpaperManager.getInstance(context);

            if (path.toLowerCase().startsWith("http") ) {
                AsynchUtil.runAsynchronously(new Runnable() {
                    @Override
                    public void run() {
                        asyncSetWallpaper(finalPath);
                    }
                });
            } else {
                drawable = MediaUtil.getBitmapDrawable(container.$form(), path);
                // convert drawable to bitmap
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

                myWallpaperManager.setBitmap(bitmap);
                AfterAction(true, "Wallpaper set", "SetWallpaper");

            }

        } catch (IOException e) {
            AfterAction(false, "Unable to set wallpaper" + e.getMessage(), "SetWallpaper");
        }
    }

    private void asyncSetWallpaper(String path) {
        Bitmap mybitmap = null;
        android.app.WallpaperManager myWallpaperManager = android.app.WallpaperManager.getInstance(context);


        try {
            if (path.toLowerCase().startsWith("http")) {
                mybitmap = Picasso.with(context).load(path).get();
                myWallpaperManager.setBitmap(mybitmap);
                AfterAction(true, "Wallpaper set", "SetWallpaper");
            } else if (path.toLowerCase().startsWith("//")) {
                if (form instanceof ReplForm) {
                    path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AppInventor/assets/" + path.substring(2);
                } else {
                    path = "android_asset/" + path.substring(2);
                }
//                String temp = "android_asset/";
//
//                if (form instanceof ReplForm) {
//                    temp = Environment.getExternalStorageDirectory().getPath() + "/AppInventor/data/";
//                }
                mybitmap = Picasso.with(context).load("file:///" + path).get(); // + path.substring(2)).get();
                myWallpaperManager.setBitmap(mybitmap);
                AfterAction(true, "Wallpaper set", "SetWallpaper");
            } else if (path.toLowerCase().startsWith("/")) {
                String baseDir = Environment.getExternalStorageDirectory().getPath();
                if (path.startsWith(baseDir)) {
                    path = path.replace(baseDir, "");
                }
                path = baseDir + path;
                mybitmap = Picasso.with(context).load(new java.io.File(path)).get();
                myWallpaperManager.setBitmap(mybitmap);
                AfterAction(true, "Wallpaper set", "SetWallpaper");
            }

        } catch (IOException e) {
            AfterAction(false, "Unable to set wallpaper" + e.getMessage()+ "::::" + Environment.getExternalStorageDirectory().getPath(), "SetWallpaper");
        }
    }
    @SimpleFunction(description = "Remove any currently set system wallpaper, reverting to the system's built-in wallpaper.")
    public void Clear() {
        try {
            android.app.WallpaperManager myWallpaperManager = android.app.WallpaperManager.getInstance(context);
            myWallpaperManager.clear();
            AfterAction(true, "Wallpaper cleared", "Clear");
        } catch (IOException e) {
            AfterAction(false, "Unable to clear wallpaper:" + e.getMessage(), "Clear");
        }
    }

    // Gotta call using async. IF using Screen1.Initialize to GetImage is called, IF not called using async,
    // then AfterAction gets invoked, but it won't actually display the image. Including in async is making it to work
    @SimpleFunction(description = "Gets the image path to current wallpaper. " +
            "The wallpaper is always stored on root SD card a /wallpaper.jpg." +
            "This block will trigger AfterAction and will pass the full path to wallpaper.")
    public void GetWallpaper() {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncGetWallpaper();
            }
        });

    }

    private void asyncGetWallpaper() {
        try {
            Log.d(LOG_TAG, "GetWallpaper invoked");
            android.app.WallpaperManager myWallpaperManager = android.app.WallpaperManager.getInstance(context);
            Drawable drawable = myWallpaperManager.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Log.d(LOG_TAG, "GetWallpaper Got bitmap");

            //no subfolder; otherwise, we'll have to create subfolder
            java.io.File file = new java.io.File(Environment.getExternalStorageDirectory() + "/wallpaper.jpg");
            boolean created = file.createNewFile();
            Log.d(LOG_TAG, "GetWallpaper created file");

            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
            ostream.flush();
            ostream.close();
            Log.d(LOG_TAG, "GetWallpaper wrote bitmpa to root sd card");

            Log.d(LOG_TAG, "Invoking AfterAction with success with getAbsolutePath of:" + file.getAbsolutePath());
            AfterAction(true, file.getAbsolutePath(), "GetWallpaper");
        } catch (Exception e) {
            Log.d(LOG_TAG, "Invoking AfterAction with fail");
            AfterAction(false, "Unable to get current wallpaper:" + e.getMessage(), "GetWallpaper");
        }
    }

    @SimpleEvent(description = "Triggered after an actions such as setting wallpaper has occured. " +
            "If wasSuccess=true, then message will have full path to wallpaper image. " +
            "For example to display wallpaper into Image, do IF wasSuccess = true then Image.Picture = message")
    public void AfterAction(final boolean wasSuccess, final String message, final String action) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                EventDispatcher.dispatchEvent(WallpaperManager.this, "AfterAction", wasSuccess, message, action);
            }
        });
    }


}



