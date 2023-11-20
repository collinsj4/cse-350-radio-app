package com.app.vaporwave.utils;

import static com.app.vaporwave.Config.USE_LEGACY_GDPR_EU_CONSENT;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.solodroid.ads.sdk.util.Constant.IRONSOURCE;

import android.app.Activity;
import android.view.View;

import com.app.vaporwave.BuildConfig;
import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.database.prefs.AdsPref;
import com.app.vaporwave.database.prefs.SharedPref;
import com.solodroid.ads.sdk.format.AdNetwork;
import com.solodroid.ads.sdk.format.BannerAd;
import com.solodroid.ads.sdk.format.InterstitialAd;
import com.solodroid.ads.sdk.format.NativeAd;
import com.solodroid.ads.sdk.format.NativeAdFragment;
import com.solodroid.ads.sdk.gdpr.GDPR;
import com.solodroid.ads.sdk.gdpr.LegacyGDPR;

public class AdsManager {

    Activity activity;
    AdNetwork.Initialize adNetwork;
    BannerAd.Builder bannerAd;
    InterstitialAd.Builder interstitialAd;
    NativeAd.Builder nativeAd;
    NativeAdFragment.Builder nativeAdView;
    SharedPref sharedPref;
    AdsPref adsPref;
    LegacyGDPR legacyGDPR;
    GDPR gdpr;

    public AdsManager(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
        this.adsPref = new AdsPref(activity);
        this.legacyGDPR = new LegacyGDPR(activity);
        this.gdpr = new GDPR(activity);
        adNetwork = new AdNetwork.Initialize(activity);
        bannerAd = new BannerAd.Builder(activity);
        interstitialAd = new InterstitialAd.Builder(activity);
        nativeAd = new NativeAd.Builder(activity);
        nativeAdView = new NativeAdFragment.Builder(activity);
    }

    public void initializeAd() {
        adNetwork.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setStartappAppId(adsPref.getStartappAppId())
                .setUnityGameId(adsPref.getUnityGameId())
                .setIronSourceAppKey(adsPref.getIronSourceAppKey())
                .setDebug(BuildConfig.DEBUG)
                .build();
    }

    public void loadBannerAd(int placement) {
        bannerAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobBannerId(adsPref.getAdMobBannerId())
                .setGoogleAdManagerBannerId(adsPref.getAdManagerBannerId())
                .setUnityBannerId(adsPref.getUnityBannerPlacementId())
                .setAppLovinBannerId(adsPref.getAppLovinBannerAdUnitId())
                .setAppLovinBannerZoneId(adsPref.getAppLovinBannerZoneId())
                .setIronSourceBannerId(adsPref.getIronSourceBannerId())
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setPlacementStatus(placement)
                .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                .build();
    }

    public void loadInterstitialAd(int placement, int interval) {
        interstitialAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobInterstitialId(adsPref.getAdMobInterstitialId())
                .setGoogleAdManagerInterstitialId(adsPref.getAdManagerInterstitialId())
                .setUnityInterstitialId(adsPref.getUnityInterstitialPlacementId())
                .setAppLovinInterstitialId(adsPref.getAppLovinInterstitialAdUnitId())
                .setAppLovinInterstitialZoneId(adsPref.getAppLovinInterstitialZoneId())
                .setIronSourceInterstitialId(adsPref.getIronSourceInterstitialId())
                .setInterval(interval)
                .setPlacementStatus(placement)
                .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                .build();
    }

    public void loadNativeAd(int placement) {
        nativeAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobNativeId(adsPref.getAdMobNativeId())
                .setAdManagerNativeId(adsPref.getAdManagerNativeId())
                .setAppLovinNativeId(adsPref.getAppLovinNativeAdManualUnitId())
                .setPlacementStatus(placement)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                .build();
    }

    public void loadNativeAdView(View view, int placement) {
        nativeAdView.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobNativeId(adsPref.getAdMobNativeId())
                .setAdManagerNativeId(adsPref.getAdManagerNativeId())
                .setAppLovinNativeId(adsPref.getAppLovinNativeAdManualUnitId())
                .setPlacementStatus(placement)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                .setView(view)
                .setPadding(
                        activity.getResources().getDimensionPixelSize(R.dimen.padding_medium),
                        activity.getResources().getDimensionPixelSize(R.dimen.padding_medium),
                        activity.getResources().getDimensionPixelSize(R.dimen.padding_medium),
                        activity.getResources().getDimensionPixelSize(R.dimen.padding_small)
                )
                .build();
    }

    public void loadNativeAdViewRadio(View view, int placement) {
        if (Config.NATIVE_AD_STYLE_ON_RADIO_LIST.equals("radio")) {
            nativeAdView.setAdStatus(adsPref.getAdStatus())
                    .setAdNetwork(adsPref.getAdType())
                    .setBackupAdNetwork(adsPref.getBackupAds())
                    .setAdMobNativeId(adsPref.getAdMobNativeId())
                    .setAdManagerNativeId(adsPref.getAdManagerNativeId())
                    .setAppLovinNativeId(adsPref.getAppLovinNativeAdManualUnitId())
                    .setPlacementStatus(placement)
                    .setDarkTheme(sharedPref.getIsDarkTheme())
                    .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                    .setNativeAdStyle(Config.NATIVE_AD_STYLE_ON_RADIO_LIST)
                    .setView(view)
                    .build();
        } else {
            nativeAdView.setAdStatus(adsPref.getAdStatus())
                    .setAdNetwork(adsPref.getAdType())
                    .setBackupAdNetwork(adsPref.getBackupAds())
                    .setAdMobNativeId(adsPref.getAdMobNativeId())
                    .setAdManagerNativeId(adsPref.getAdManagerNativeId())
                    .setAppLovinNativeId(adsPref.getAppLovinNativeAdManualUnitId())
                    .setPlacementStatus(placement)
                    .setDarkTheme(sharedPref.getIsDarkTheme())
                    .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                    .setNativeAdStyle(Config.NATIVE_AD_STYLE_ON_RADIO_LIST)
                    .setView(view).setPadding(
                            activity.getResources().getDimensionPixelSize(R.dimen.padding_medium),
                            activity.getResources().getDimensionPixelSize(R.dimen.padding_medium),
                            activity.getResources().getDimensionPixelSize(R.dimen.padding_medium),
                            activity.getResources().getDimensionPixelSize(R.dimen.padding_small)
                    )
                    .build();
        }
    }

    public void showInterstitialAd() {
        interstitialAd.show();
    }

    public void destroyBannerAd() {
        bannerAd.destroyAndDetachBanner();
    }

    public void resumeBannerAd(int placement) {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && !adsPref.getIronSourceBannerId().equals("0")) {
            if (adsPref.getAdType().equals(IRONSOURCE) || adsPref.getBackupAds().equals(IRONSOURCE)) {
                loadBannerAd(placement);
            }
        }
    }

    public void updateConsentStatus() {
        if (USE_LEGACY_GDPR_EU_CONSENT) {
            legacyGDPR.updateLegacyGDPRConsentStatus(adsPref.getAdMobPublisherId(), sharedPref.getBaseUrl() + "/privacy.php");
        } else {
            gdpr.updateGDPRConsentStatus();
        }
    }

}
