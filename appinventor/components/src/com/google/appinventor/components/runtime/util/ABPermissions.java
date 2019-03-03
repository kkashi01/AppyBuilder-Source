package com.google.appinventor.components.runtime.util;

/**
 * Based on https://gist.github.com/mtsahakis/bd54dc595f8cf170eb1bce17f31722b5
 */

// also see here. Made some updates to above based on this: https://developer.android.com/training/permissions/requesting
// https://github.com/marcoscgdev/EasyPermissions
// see here too: https://developer.android.com/training/permissions/requesting

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
//import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/*
 * List of dangerous permission:
 * READ_CALENDAR
 * WRITE_CALENDAR
 * CAMERA
 * READ_CONTACTS
 * WRITE_CONTACTS
 * GET_ACCOUNTS
 * ACCESS_FINE_LOCATION
 * ACCESS_COARSE_LOCATION
 * RECORD_AUDIO
 * READ_PHONE_STATE
 * READ_PHONE_NUMBERS
 * CALL_PHONE
 * ANSWER_PHONE_CALLS
 * READ_CALL_LOG
 * WRITE_CALL_LOG
 * ADD_VOICEMAIL
 * USE_SIP
 * PROCESS_OUTGOING_CALLS
 * BODY_SENSORS
 * SEND_SMS
 * RECEIVE_SMS
 * READ_SMS
 * RECEIVE_WAP_PUSH
 * RECEIVE_MMS
 * READ_EXTERNAL_STORAGE
 * WRITE_EXTERNAL_STORAGE
 */
public class ABPermissions {

    // Calendar group.
    public static final String READ_CALENDAR = Manifest.permission.READ_CALENDAR;
    public static final String WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR;

    // Camera group.
    public static final String CAMERA = Manifest.permission.CAMERA;

    // Contacts group.
    public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    public static final String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;
    public static final String READ_PROFILE = "android.permission.READ_CONTACTS";
    public static final String WRITE_PROFILE = "android.permission.WRITE_CONTACTS";

    // Location group.
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    // Microphone group.
    public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    // Phone group.
    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final String READ_CALL_LOG = Manifest.permission.READ_CALL_LOG;
    public static final String WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG;
    public static final String ADD_VOICEMAIL = Manifest.permission.ADD_VOICEMAIL;
    public static final String USE_SIP = Manifest.permission.USE_SIP;
    public static final String PROCESS_OUTGOING_CALLS = Manifest.permission.PROCESS_OUTGOING_CALLS;

    // Sensors group.
    public static final String BODY_SENSORS = Manifest.permission.BODY_SENSORS;
//    public static final String USE_FINGERPRINT = Manifest.permission.USE_FINGERPRINT;

    // SMS group.
    public static final String SEND_SMS = Manifest.permission.SEND_SMS;
    public static final String RECEIVE_SMS = Manifest.permission.RECEIVE_SMS;
    public static final String READ_SMS = Manifest.permission.READ_SMS;
    public static final String RECEIVE_WAP_PUSH = Manifest.permission.RECEIVE_WAP_PUSH;
    public static final String RECEIVE_MMS = Manifest.permission.RECEIVE_MMS;
    public static final String READ_CELL_BROADCASTS = "android.permission.READ_CELL_BROADCASTS";

    // Bookmarks group.
    public static final String READ_HISTORY_BOOKMARKS = "com.android.browser.permission.READ_HISTORY_BOOKMARKS";
    public static final String WRITE_HISTORY_BOOKMARKS = "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS";

    // Storage group.
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String LOG_TAG = "ABPermissions";

    public static boolean useRunTimePermissions() {
        return Build.VERSION.SDK_INT >= 23;
    }

    public static boolean hasPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        if (useRunTimePermissions()) {
            activity.requestPermissions(permissions, requestCode);
        }
    }


    public static boolean shouldShowRational(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    public static boolean shouldAskForPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return !hasPermission(activity, permission) &&
                    (!hasAskedForPermission(activity, permission) ||
                            shouldShowRational(activity, permission));
        }
        return false;
    }

    public static void goToAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static boolean hasAskedForPermission(Activity activity, String permission) {
        return PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getBoolean(permission, false);
    }

    public static void markedPermissionAsAsked(Activity activity, String permission) {
        PreferenceManager
                .getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(permission, true)
                .apply();
    }

    public static String[] getAppPermission(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            PackageInfo packageInfo = null;
            packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);
            String[] requestedPermissions = packageInfo.requestedPermissions;
            return requestedPermissions;
        } catch (Exception  e) {
            Log.d(LOG_TAG, "Unable to get app permissions. Error is:" + e.getLocalizedMessage());
            return new String[] {};
        }
    }

}
