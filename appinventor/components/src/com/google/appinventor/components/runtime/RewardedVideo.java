// Copyright 2016-2020 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html
package com.google.appinventor.components.runtime;

//import android.widget.Toast;

import android.os.Bundle;
import android.util.Log;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AdMobUtil;
import com.google.appinventor.components.runtime.util.conscent.ConsentInfoUpdateListener;
import com.google.appinventor.components.runtime.util.conscent.ConsentInformation;
import com.google.appinventor.components.runtime.util.conscent.ConsentStatus;
import com.google.appinventor.components.runtime.util.conscent.GDPRUtils;

@DesignerComponent(version = YaVersion.ADMOB_REWARD_VIDEO_COMPONENT_VERSION,
        description = "add description in OdeMessage ",
        category = ComponentCategory.MONETIZE,
        nonVisible = true,
        iconName = "images/rewardedvideo.png")
@SimpleObject
@UsesLibraries(libraries = "google-play-services.jar,gson-2.1.jar,firebase.jar")
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.ACCESS_NETWORK_STATE" )
public class RewardedVideo extends AndroidNonvisibleComponent implements Component, RewardedVideoAdListener {

    public String adUnitId;
    private boolean enableTesting = false;
    private boolean adEnabled = true;
    private RewardedVideoAd mAd;

    private static final String LOG_TAG = "RewardedVideo";
    protected final ComponentContainer container;

    public RewardedVideo(ComponentContainer container) {
        super(container.$form());
        this.container = container;

        mAd = MobileAds.getRewardedVideoAdInstance(container.$context());
        mAd.setRewardedVideoAdListener(this);

        this.adEnabled = true;
    }

    @SimpleEvent(description = "Triggered when AD fails to load")
    public void AdFailedToLoad(int errCode, String errMessage) {
        EventDispatcher.dispatchEvent(this, "AdFailedToLoad", errCode, errMessage);
    }

    @SimpleEvent(description = "Called when the user is about to return to the application after clicking on an ad")
    public void AdClosed() {
        EventDispatcher.dispatchEvent(this, "AdClosed");
    }

    @SimpleEvent(
            description = "Called when an ad leaves the application (e.g., to go to the browser). ")
    public void AdLeftApplication() {
        EventDispatcher.dispatchEvent(this, "AdLeftApplication");
    }

    @SimpleEvent(description = "Called when an ad is received")
    public void AdLoaded() {
        EventDispatcher.dispatchEvent(this, "AdLoaded");
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false) //we don't want the blocks for this
    public String AdUnitID() {
        return this.adUnitId;
    }

    //NOTE: DO NOT allow setting in the blocks-editor. It can be set ONLY ONCE
    @DesignerProperty(defaultValue = "AD-UNIT-ID", editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty(userVisible = false)  //we can't keep setting adUnitId into ad. Therefore, i have disabled the block.
    public void AdUnitID(String adUnitId) {
        this.adUnitId = adUnitId;

        LoadAd();   //NOTE: setAdUnitId has to be done first. If we load ad, it will cause ambigeous runtime exception. DO NOT LoadAd here
    }

    @SimpleProperty(userVisible = true, description = "For debugging / development purposes flag all ad requests as tests, " +
            "but set to false for production builds. Will take effect when you use LoadAd block.")
    public void TestMode(boolean enabled) {
        this.enableTesting = enabled;
        Log.d(LOG_TAG, "flipping the test mode to: " + this.enableTesting);

    }

    // sample code here: https://developers.google.com/admob/android/rewarded-video
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean TestMode() {
        return enableTesting;
    }

    @SimpleFunction(description = "Loads a new ad.")
    public void LoadAd() {
        if (!adEnabled) {
            return;
        }
        Log.d(LOG_TAG, "The test mode status is: " + this.enableTesting);

        if (this.enableTesting) {
            Log.d(LOG_TAG, "Test mode");
            String device = AdMobUtil.guessSelfDeviceId(container.$context());

            mAd.loadAd(adUnitId, new AdRequest.Builder().addTestDevice(device).build());

            return;
        }

        if (!enableTesting) {
            Bundle extras = new Bundle();
            // non-personalized = 1: https://develpers.google.com/admob/android/eu-consent#forward_consent_to_the_google_mobile_ads_sdk
            extras.putString("npa", !Boolean.valueOf(isPersonalized)? "1" : "0");  // 1 = true

            AdRequest.Builder builder = new AdRequest.Builder();
            builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);

            mAd.loadAd(adUnitId, builder.build());

        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "If true, device that will receive test ads. " +
            "You should utilize this property during development to avoid generating false impressions")
    public void AdEnabled(boolean enabled) {
        this.adEnabled = enabled;
    }

    /**
     * Returns status of AdEnabled
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean AdEnabled() {
        return adEnabled;
    }
    // Required to reward the user.
    @Override
    public void onRewarded(RewardItem reward) {
        Rewarded(reward.getType(), reward.getAmount());
    }

    // The following listener methods are optional.
    @Override
    public void onRewardedVideoAdLeftApplication() {
        AdLeftApplication();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        AdClosed();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        // ERROR_CODE_INTERNAL_ERROR, ERROR_CODE_INVALID_REQUEST, ERROR_CODE_NETWORK_ERROR, or ERROR_CODE_NO_FILL
        String errMessage;
        switch (errorCode) {
            case 0 :
                errMessage = "Internal Error";
                break;
            case 1:
                errMessage = "Invalid Request";
                break;
            case 2:
                errMessage = "Network Error";
                break;
            case 3:
                errMessage = "Ad was not Filled";
                break;
            default: errMessage = "Unknown error. Ad Failed To Load";
                break;
        }
        AdFailedToLoad(errorCode, errMessage);
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        AdLoaded();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        AdOpened();
    }

    @SimpleEvent(description = "Ad was opened by user")
    public void AdOpened() {
        EventDispatcher.dispatchEvent(this, "AdOpened");
    }

    @Override
    public void onRewardedVideoStarted() {
        AdOpened();
    }

    @SimpleFunction(description = "It will show the Video")
    public void ShowAd() {
        if (mAd.isLoaded()) {
            mAd.show();
            return;
        }

        String msg = "Video Ad is not ready to show. Make sure AD is loaded";
        AdFailedToShow(msg);
    }

    @SimpleEvent(description = "Called when an an attempt was made to display the ad, but the ad was not ready to display")
    public void AdFailedToShow(String message) {
        EventDispatcher.dispatchEvent(this, "AdFailedToShow", message);
    }

    @SimpleEvent(description = "User watched video and should be rewarded")
    public void Rewarded(String type, int amount) {
        EventDispatcher.dispatchEvent(this, "Rewarded", type, amount);
        // Reward the user.
    }


    //============= censcent logic
    private String isPersonalized = "true";

    @SimpleFunction (description = "A block to determine if app-user is in Europe. If result is true, use RequestConsentStatus to determine status of consent")
    public boolean IsEuropeanUser() {
        return GDPRUtils.isEuropeanUser();
    }

    @SimpleFunction(description = "This block will revoke (cancel) the user consent")
    public void RevokeConsent() {
        ConsentInformation.getInstance(container.$context()).reset();
    }

    @SimpleFunction(description = "This block will determine the status of user-consent. It will trigger ConsentStatusLoaded block")
    public void RequestConsentStatus() {
        String temp = adUnitId.trim();
        if (temp.isEmpty()) {
            ConsentStatusLoaded("unknown", getConsentInfo().isRequestLocationInEeaOrUnknown(), "AdUnitId is invalid");
            return;
        }

        String publisherId;
        // split it
        try {
            publisherId = adUnitId.split("-")[3].split("/")[0];
        } catch (Exception e) {
            ConsentStatusLoaded("unknown", getConsentInfo().isRequestLocationInEeaOrUnknown(), "AdUnitId is invalid");
            return;
        }

        // all seems to be good. Get list of publisherIDs in for of: "pub-0123456789012345"
        String[] publisherIds = {publisherId};

        ConsentInformation.getInstance(container.$context()).requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
                ConsentStatusLoaded( ""+ consentStatus.isPersonalConsent(), getConsentInfo().isRequestLocationInEeaOrUnknown(), consentStatus.name());
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
                ConsentStatusLoaded( "false", getConsentInfo().isRequestLocationInEeaOrUnknown(), errorDescription);
            }
        });
    }

    private ConsentInformation getConsentInfo() {
        return ConsentInformation.getInstance(container.$context());
    }
    @SimpleEvent(description = "Triggered after RequestConsentStatus block is invoked. It determines the status of a user's consent. " +
            "Possible message values are personalized, non-personalized or unknown. If unknown, user has not given consent yet. " +
            "For this, you need to get user consent. ")
    public void ConsentStatusLoaded(final String isPersonalized, boolean isEuropeanUser, final String message ) {
        this.isPersonalized = isPersonalized;
        EventDispatcher.dispatchEvent(this, "ConsentStatusLoaded", isPersonalized, isEuropeanUser, message);
    }

    @SimpleFunction(description = "Use this block to set consent type. ")
    public void SetConsent(boolean isPersonalized) {
        this.isPersonalized = ""+ isPersonalized;
        ConsentInformation.getInstance(container.$context()).setConsentStatus(isPersonalized? ConsentStatus.PERSONALIZED : ConsentStatus.NON_PERSONALIZED);
    }


}
