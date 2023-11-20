package com.app.vaporwave.activities;

import static com.app.vaporwave.utils.Constant.LOCALHOST_ADDRESS;
import static com.app.vaporwave.utils.Constant.THEME_DARK;
import static com.app.vaporwave.utils.Constant.THEME_LIGHT;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.vaporwave.BuildConfig;
import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.callbacks.CallbackSettings;
import com.app.vaporwave.database.prefs.AdsPref;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.models.Settings;
import com.app.vaporwave.rests.RestAdapter;
import com.app.vaporwave.utils.Constant;
import com.app.vaporwave.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    public static final String TAG = "ActivitySplash";
    ProgressBar progressBar;
    AdsPref adsPref;
    ThemePref themePref;
    SharedPref sharedPref;
    ImageView imgSplash;
    Call<CallbackSettings> callbackCall = null;
    Settings ads;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_splash);

        sharedPref = new SharedPref(this);
        themePref = new ThemePref(this);

        adsPref = new AdsPref(this);

        imgSplash = findViewById(R.id.img_splash);
        if (themePref.getCurrentTheme() == THEME_LIGHT) {
            imgSplash.setImageResource(R.drawable.splash_light);
            Tools.lightStatusBar(this, true);
        } else if (themePref.getCurrentTheme() == THEME_DARK) {
            imgSplash.setImageResource(R.drawable.splash_dark);
            Tools.darkNavigation(this);
        } else {
            imgSplash.setImageResource(R.drawable.splash_primary);
            Tools.primaryNavigation(this);
        }

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        adsPref = new AdsPref(this);
        if (adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            Application application = getApplication();
            if (adsPref.getAdType().equals(ADMOB)) {
                if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                    ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::requestConfig);
                } else {
                    requestConfig();
                }
            } else if (adsPref.getAdType().equals(GOOGLE_AD_MANAGER)) {
                if (!adsPref.getAdManagerAppOpenAdId().equals("0")) {
                    ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::requestConfig);
                } else {
                    requestConfig();
                }
            } else {
                requestConfig();
            }
        } else {
            requestConfig();
        }

    }

    @SuppressWarnings("ConstantConditions")
    private void requestConfig() {
        if (Config.SERVER_KEY.contains("XXXXX")) {
            new AlertDialog.Builder(this)
                    .setTitle("App not configured")
                    .setMessage("Please put your Server Key and Rest API Key from settings menu in your admin panel to AppConfig, you can see the documentation for more detailed instructions.")
                    .setPositiveButton(getString(R.string.option_ok), (dialogInterface, i) -> startMainActivity())
                    .setCancelable(false)
                    .show();
        } else {
            String decode = Tools.decodeBase64(Config.SERVER_KEY);
            String data = Tools.decrypt(decode);
            String[] results = data.split("_applicationId_");
            String apiUrl = results[0].replace("http://localhost", LOCALHOST_ADDRESS);
            String applicationId = results[1];
            sharedPref.saveConfig(apiUrl, applicationId);

            if (applicationId.equals(BuildConfig.APPLICATION_ID)) {
                if (Tools.isConnect(this)) {
                    requestAPI(apiUrl);
                } else {
                    startMainActivity();
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Whoops! invalid server key or applicationId, please check your configuration")
                        .setPositiveButton(getString(R.string.option_ok), (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void requestAPI(String apiUrl) {
        this.callbackCall = RestAdapter.createAPI(apiUrl).getSettings(Config.REST_API_KEY);
        this.callbackCall.enqueue(new Callback<CallbackSettings>() {
            public void onResponse(@NonNull Call<CallbackSettings> call, @NonNull Response<CallbackSettings> response) {
                CallbackSettings resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    ads = resp.settings;
                    adsPref.saveAds(
                            ads.ad_status.replace("on", "1"),
                            ads.ad_type,
                            ads.backup_ads,
                            ads.admob_publisher_id,
                            ads.admob_app_id,
                            ads.admob_banner_unit_id,
                            ads.admob_interstitial_unit_id,
                            ads.admob_native_unit_id,
                            ads.admob_app_open_ad_unit_id,
                            ads.ad_manager_banner_unit_id,
                            ads.ad_manager_interstitial_unit_id,
                            ads.ad_manager_native_unit_id,
                            ads.ad_manager_app_open_ad_unit_id,
                            ads.startapp_app_id,
                            ads.unity_game_id,
                            ads.unity_banner_placement_id,
                            ads.unity_interstitial_placement_id,
                            ads.applovin_banner_ad_unit_id,
                            ads.applovin_interstitial_ad_unit_id,
                            ads.applovin_native_ad_manual_unit_id,
                            ads.applovin_banner_zone_id,
                            ads.applovin_interstitial_zone_id,
                            ads.ironsource_app_key,
                            ads.ironsource_banner_id,
                            ads.ironsource_interstitial_id,
                            ads.interstitial_ad_interval,
                            ads.native_ad_interval,
                            ads.native_ad_index
                    );

                    sharedPref.saveCredentials(
                            ads.fcm_notification_topic,
                            ads.onesignal_app_id,
                            ads.more_apps_url,
                            ads.privacy_policy
                    );

                    Log.d("ActivitySplash", "success load config");
                }
                startMainActivity();
            }

            public void onFailure(@NonNull Call<CallbackSettings> call, @NonNull Throwable th) {
                Log.e(TAG, "onFailure");
                startMainActivity();
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, Constant.DELAY_SPLASH);
    }

}
