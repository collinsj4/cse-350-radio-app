package com.app.vaporwave;

import com.app.vaporwave.utils.Constant;

public class Config {


    public static final String SERVER_KEY = "WVVoU01HTklUVFpNZVRreVdWaENkbU51Wkdoa2JWVjFXbGhTYkdKSWFIZGpiVlo2WTNrMWFtSXlNSFpZTWtaM1kwZDRjRmt5UmpCaFZ6bDFVMWRTWmxreU9YUk1iVVozWTBNMU1sbFlRblpqYm1Sb1pHMVZQUT09";


    public static final String REST_API_KEY = "cda11lHY0ZafN2nrti4U5QAKMDhTw7Czm1xoSsyVLduvRegkqE";

    //display app in simple mode without home menu
    public static final boolean SIMPLE_MODE = false;

    //display social page in the navigation menu
    public static final boolean DISPLAY_SOCIAL_IN_NAVIGATION_MENU = true;

    //display blur radio image when the player view is expanded
    public static final boolean DISPLAY_RADIO_BACKGROUND_BLUR_IMAGE = false;

    //display metadata of song played from radio station if it is available
    public static final boolean DISPLAY_SONG_METADATA = true;

    //display image album art of song played from radio station if it is available
    //if song metadata is disabled, album art will also disabled automatically
    public static final boolean DISPLAY_ALBUM_ART_METADATA = true;

    //number of radios for each category
    public static final boolean DISPLAY_RADIO_COUNT_ON_CATEGORY_LIST = true;

    //radio streaming timeout connection, in milliseconds
    public static final boolean ENABLE_RADIO_TIMEOUT = true;
    public static final int RADIO_TIMEOUT_CONNECTION = 10000;

    //load more pagination
    public static final int PAGINATION = 10;

    //native ad style radio list : radio or default
    public static final String NATIVE_AD_STYLE_ON_RADIO_LIST = "radio";

    //number of columns in a row category
    public static final int CATEGORY_COLUMN_COUNT = 2;

    //radio will stop when receiving a phone call and will resume when the call ends
    public static final boolean RESUME_RADIO_ON_PHONE_CALL = false;

    //set true if you want to enable RTL (Right To Left) mode, e.g : Arabic Language
    public static final boolean ENABLE_RTL_MODE = false;

    //default theme in the first launch : Constant.THEME_PRIMARY or Constant.THEME_LIGHT or Constant.THEME_DARK
    public static final int DEFAULT_THEME = Constant.THEME_PRIMARY;

    //GDPR EU Consent
    public static final boolean USE_LEGACY_GDPR_EU_CONSENT = true;

    //push notification handle when open url
    public static final boolean OPEN_NOTIFICATION_LINK_IN_EXTERNAL_BROWSER = true;

    //social menu open url
    public static final boolean OPEN_SOCIAL_MENU_IN_EXTERNAL_BROWSER = true;

    //enable or disable ads for each screen : 1 or 0
    public static final int BANNER_HOME = 1;
    public static final int INTERSTITIAL_RADIO_LIST = 1;
    public static final int INTERSTITIAL_CATEGORY_LIST = 1;
    public static final int NATIVE_AD_HOME = 1;
    public static final int NATIVE_AD_RADIO_LIST = 1;
    public static final int NATIVE_AD_EXIT_DIALOG = 1;
    public static final int APP_OPEN_AD_ON_RESUME = 1;

}