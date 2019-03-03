// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
package com.google.appinventor.components.runtime;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import java.util.List;

/**
 * Contains various helper methods
 *
 */
@DesignerComponent(version = YaVersion.KITCHENSINK_COMPONENT_VERSION,
        description = "xxxxxxxxx Update odeMessage",
        category = ComponentCategory.CONNECTIVITY,
        nonVisible = true,
        iconName = "images/network.png")
@UsesPermissions(permissionNames = "android.permission.ACCESS_NETWORK_STATE")
@SimpleObject
public class NetworkManager extends AndroidNonvisibleComponent implements Component {
    private static final String LOG_TAG = "NetworkManager";
    private final Context context;

    /**
     * Creates a new NetworkManager component.
     *
     * @param container the Form that this component is contained in.
     */
    public NetworkManager(ComponentContainer container) {
        super(container.$form());
        context = (Context) container.$context();
    }

    private static boolean isConnectionFast(int type, int subType){
        if(type==ConnectivityManager.TYPE_WIFI){
            return true;
        }else if(type==ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        }else{
            return false;
        }
    }

    @SimpleFunction (description = "Returns true if connection is through WiFi")
    public boolean IsWiFiConnection() {
        NetworkInfo info = getNetwork();
        return (info != null && info.isConnectedOrConnecting() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    @SimpleFunction (description = "Returns true if connection is through Mobile")
    public boolean IsMobileConnection() {
        NetworkInfo info = getNetwork();
        return (info != null && info.isConnectedOrConnecting() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    @SimpleFunction (description = "Returns true if using a fast connection")
    public boolean IsFastConnection() {
        NetworkInfo info = getNetwork();
        return (info != null && info.isConnectedOrConnecting() && isConnectionFast(info.getType(),info.getSubtype()));
    }

    @SimpleFunction (description = "Returns true if using using roaming")
    public boolean IsRoaming() {
        NetworkInfo info = getNetwork();
        return (info != null && info.isConnectedOrConnecting() && info.isRoaming());
    }

    @SimpleFunction (description = "Indicates whether network connectivity exists and it is possible to establish connections and pass data.")
    public boolean IsConnected() {
        NetworkInfo info = getNetwork();
        return info!=null && info.isConnectedOrConnecting();
    }

    @SimpleFunction (description = " describe the type of the network, for example WIFI or MOBILE")
    public String GetConnectionType() {
        NetworkInfo info = getNetwork();
        if (info==null) return "UNABLE TO GET CONNECTION TYPE";
        return info.getTypeName();
    }


    private NetworkInfo getNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }


    @SimpleProperty(description = "Checks to see if device is GPS enabled")
    public boolean IsGPSEnabledDevice()
    {
        final LocationManager mgr = (LocationManager) form.getSystemService(Context.LOCATION_SERVICE);
        if ( mgr == null ) return false;
        final List<String> providers = mgr.getAllProviders();
        return providers != null && providers.contains(LocationManager.GPS_PROVIDER);
    }

    @SimpleProperty(description = "Checks to see if device is GPS enabled and if so, checks to see if GPS is started or not")
    public boolean IsGPSEnabled()
    {
        if (!IsGPSEnabledDevice()) {
            return false;
        }

        //http://code.google.com/p/krvarma-android-samples/source/browse/trunk/CheckGPS/src/com/varma/samples/checkgps/MainActivity.java
        final LocationManager manager = (LocationManager) form.getSystemService(Context.LOCATION_SERVICE);

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @SimpleProperty(description = "Starts up the GPS configuration activity, giving user option to turn turn on the GPS")
    public boolean StartGPSOptions()
    {
        //NOTE: When method has no arguments, the annotation processor wants us to return a type and cannot be void;
        // hence, for now, using boolean
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        form.startActivity(intent);
        //TODO: Should we do startActivityForResult so thta we could check status of user's gps selection?
//        startActivityForResult(intent, REQUEST_CODE);
        //TODO: we can now implement method such as:
        /*
        @Override
                protected void onActivityResult(int requestCode, int resultCode, Intent data)
                {
                        super.onActivityResult(requestCode, resultCode, data);
                        if(requestCode == REQUEST_CODE)
                        {
                            displayGPSState(isGPSenabled());
                        }
                }

         */

        return true;
    }

}



