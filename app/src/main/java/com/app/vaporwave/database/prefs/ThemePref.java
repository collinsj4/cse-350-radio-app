package com.app.vaporwave.database.prefs;

import static com.app.vaporwave.Config.DEFAULT_THEME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class ThemePref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    @SuppressLint("CommitPrefEdits")
    public ThemePref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("theme_setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setDefaultTheme() {
        editor.putInt("app_theme", DEFAULT_THEME);
        editor.apply();
    }

    public Integer getCurrentTheme() {
        return sharedPreferences.getInt("app_theme", DEFAULT_THEME);
    }

    public void updateTheme(int position) {
        editor.putInt("app_theme", position);
        editor.apply();
    }

}
