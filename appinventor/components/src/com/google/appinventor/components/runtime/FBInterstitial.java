package com.google.appinventor.components.runtime;

import android.content.Context;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

@SimpleObject
@DesignerComponent(version = YaVersion.FB_FULL_COMPONENT_VERSION,
        description = "xxxxxxxxx Update OdeMessage",
        category = ComponentCategory.MONETIZE,
        nonVisible = true,
        iconName = "images/fbfull.png")

@UsesLibraries(libraries = "audience-network-sdk-5.1.0.jar")
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")
@UsesActivities(activities = {@ActivityElement(configChanges = "keyboardHidden|orientation|screenSize", name = "com.facebook.ads.AudienceNetworkActivity")})
public class FBInterstitial extends AndroidNonvisibleComponent implements OnDestroyListener {
    private static final String LOG_TAG = "FBInterstitial";
    private ComponentContainer container;
    private Context context;
    private InterstitialAd interstitialAd;
    private String placementId;

    public FBInterstitial(ComponentContainer componentContainer) {
        super(componentContainer.$form());
        container = componentContainer;
        context = componentContainer.$context();
//        componentContainer.$form().registerForOnDestroy((OnDestroyListener) this);

        container.$form().registerForOnDestroy(this);

    }

    @SimpleEvent(description = "Event triggered when ads are clicked")
    public void AdClicked() {
        EventDispatcher.dispatchEvent(this, "AdClicked");
    }

    @SimpleEvent(description = "Called when the user is about to return to the application after clicking on an ad")
    public void AdClosed() {
        EventDispatcher.dispatchEvent(this, "AdClosed");
    }

    @SimpleEvent(description = "Called when an ad is loaded")
    public void AdLoaded() {
        EventDispatcher.dispatchEvent(this, "AdLoaded");
    }

    @SimpleEvent(description = "Called when an ad failed to load. message will display the error and reason for failure")
    public void AdFailedToLoad(String error, String message) {
        EventDispatcher.dispatchEvent((Component) this, "AdFailedToLoad", error, message);
    }

    @SimpleEvent(description = "Called when an ad is displayed")
    public void AdDisplayed() {
        EventDispatcher.dispatchEvent((Component) this, "AdDisplayed");
    }

    ////////////////////////////////////////
    @SimpleFunction(description = "Loads a new ad")
    public void LoadAd() {
        if (placementId == null || placementId.trim().isEmpty()) {
            AdFailedToLoad("9999", "Placement ID is blank");
            return;
        }


        InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(context, placementId);

        this.interstitialAd = interstitialAd;
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {

            public void onAdClicked(Ad ad) {
                //Log.d((String) "FBInterstitial", (String) "Ad clicked");
                AdClicked();
            }

            public void onAdLoaded(Ad ad) {
                AdLoaded();
            }

            public void onError(Ad ad, AdError adError) {
                AdFailedToLoad(adError.getErrorCode() + "", adError.getErrorMessage());
            }

            public void onInterstitialDismissed(Ad ad) {
                AdClosed();
            }

            public void onInterstitialDisplayed(Ad ad) {
                AdDisplayed();
            }

            public void onLoggingImpression(Ad ad) {
            }
        };
        interstitialAd.setAdListener(interstitialAdListener);
        interstitialAd.loadAd();
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = true)
    public String PlacementID() {
        return this.placementId;
    }

    @DesignerProperty(defaultValue = "Placement-ID")
    @SimpleProperty(userVisible = true, description = "Used to set Facebook Placement ID")
    public void PlacementID(String placementId) {
        this.placementId = placementId;
    }

    @SimpleFunction(description = "Shows an ad to the user.")
    public void ShowInterstitialAd() {
        if (interstitialAd != null && interstitialAd.isAdLoaded()) {
            interstitialAd.show();
        }
    }

    @Override
    public void onDestroy() {
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
    }

}

