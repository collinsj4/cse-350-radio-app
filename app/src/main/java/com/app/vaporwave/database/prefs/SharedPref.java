package com.app.vaporwave.database.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import com.app.vaporwave.R;

public class SharedPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", false);
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public int getFirstColor() {
        return sharedPreferences.getInt("first", ContextCompat.getColor(context, R.color.colorPrimaryDark));
    }

    public int getSecondColor() {
        return sharedPreferences.getInt("second", ContextCompat.getColor(context, R.color.colorPrimary));
    }

    public void setCheckSleepTime() {
        if (getSleepTime() <= System.currentTimeMillis()) {
            setSleepTime(false, 0, 0);
        }
    }

    public void setSleepTime(Boolean isTimerOn, long sleepTime, int id) {
        editor.putBoolean("isTimerOn", isTimerOn);
        editor.putLong("sleepTime", sleepTime);
        editor.putInt("sleepTimeID", id);
        editor.apply();
    }

    public Boolean getIsSleepTimeOn() {
        return sharedPreferences.getBoolean("isTimerOn", false);
    }

    public long getSleepTime() {
        return sharedPreferences.getLong("sleepTime", 0);
    }

    public int getSleepID() {
        return sharedPreferences.getInt("sleepTimeID", 0);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void saveConfig(String api_url, String application_id) {
        editor.putString("api_url", api_url);
        editor.putString("application_id", application_id);
        editor.apply();
    }

    public String getBaseUrl() {
        return sharedPreferences.getString("api_url", "http://example.com");
    }

    public String getApplicationId() {
        return sharedPreferences.getString("application_id", "com.app.vaporwave");
    }

    public void saveCredentials(String fcm_notification_topic, String onesignal_app_id, String more_apps_url, String privacy_policy) {
        editor.putString("fcm_notification_topic", fcm_notification_topic);
        editor.putString("onesignal_app_id", onesignal_app_id);
        editor.putString("more_apps_url", more_apps_url);
        editor.putString("privacy_policy", privacy_policy);
        editor.apply();
    }

    public String getFcmNotificationTopic() {
        return sharedPreferences.getString("fcm_notification_topic", "your_recipes_app_topic");
    }

    public String getOneSignalAppId() {
        return sharedPreferences.getString("onesignal_app_id", "0");
    }

    public String getMoreAppsUrl() {
        return sharedPreferences.getString("more_apps_url", "https://vaporwaveapp.etelxpress.com/");
    }

    public String getPrivacyPolicy() {
        return sharedPreferences.getString("privacy_policy", "");
    }

    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }

//    public void setIsPlaying(boolean isPlaying) {
//        editor.putBoolean("is_playing", isPlaying);
//        editor.apply();
//    }
//
//    public boolean getIsPlaying() {
//        return sharedPreferences.getBoolean("is_playing", false);
//    }

    public void setCurrentRadioPosition(int position) {
        editor.putInt("radio_position", position);
        editor.apply();
    }

    public int getCurrentRadioPosition() {
        return sharedPreferences.getInt("radio_position", 0);
    }

}