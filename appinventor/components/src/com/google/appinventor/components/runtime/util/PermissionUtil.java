// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build.VERSION;
import android.util.Log;
import java.util.*;

import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;


public class PermissionUtil {
    private PermissionUtil() {
    }

    private static String LOG_TAG = "Permission Util";


    /**
     * Helper to check if the needed permission is granted | for >= Api 26 | since target api is > 22
     *
     * @param activity  the application activity
     */
    public static void checkRuntimePermission(final Activity activity) {
        if (VERSION.SDK_INT < 23) {
            //nothing to do
            Log.i(LOG_TAG, "No permission check needed since api level is "+VERSION.SDK_INT+" and not >= 23");
            return;
        }
        ArrayList<String> permissionList = new ArrayList<String>();
        String[] permission = getNeededPermissions(activity);

        if(permission == null || permission.length == 0) {
            Log.i(LOG_TAG, "No need to grant any permission since no needed permission was found in the manifest file.");
            return;
        }
        for (int i = 0; i < permission.length; i++) {
            if (activity.checkSelfPermission(permission[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission[i]);
            }
        }
        final String[] newPermissionsArray = permissionList.toArray(new String[permissionList.size()]);
        if(newPermissionsArray == null || newPermissionsArray.length == 0) {
            Log.i(LOG_TAG, "No need to give a permit. Maybe they were given before.");
            return;
        }
        activity.requestPermissions(newPermissionsArray, 1);
    }

    /**
     * Helper to get all needed app permissions from the application
     *
     * @param activity  the application activity
     * @return  String []
     *
     */
    public static String[] getNeededPermissions(Activity activity) {
        PackageManager packageManager = activity.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return packageInfo.requestedPermissions;
    }

    // this method doesn't seem to work
    /**
     * Helper to return a boolean if all needed app permissions was granted or not
     *
     * @param activity  the application activity
     * @return  result (boolean)
     *
     */
    public static boolean arePermissionsGranted(final Activity activity) {
        boolean result = false;
        String[] permission = getNeededPermissions(activity);

        if(permission == null || permission.length == 0) {
            Log.i(LOG_TAG, "No need to grant any permission since no needed permission was found in the manifest file.");
            return true;
        }

        result = true;
        for (int i = 0; i < permission.length; i++) {
            if (activity.shouldShowRequestPermissionRationale( permission[i])) {
                result = false;
            } else {
                if (activity.checkSelfPermission(permission[i]) != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Helper to open the current applications settings page
     *
     * @param activity  the application activity
     *
     */
    public static void appSettings(final Context activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

}