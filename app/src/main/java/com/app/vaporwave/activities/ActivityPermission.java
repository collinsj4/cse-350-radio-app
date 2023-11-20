package com.app.vaporwave.activities;

import static com.app.vaporwave.utils.Constant.PERMISSIONS_REQUEST;
import static com.app.vaporwave.utils.Constant.THEME_DARK;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.vaporwave.BuildConfig;
import com.app.vaporwave.R;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.utils.Tools;

public class ActivityPermission extends AppCompatActivity {

    Button btnAllowPermission;
    Button btnLater;
    TextView txtPermissionMessage;
    TextView txtAppVersion;
    TextView txtPrivacyPolicy;
    ThemePref themePref;
    SharedPref sharedPref;
    ScrollView scrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_permission);

        sharedPref = new SharedPref(this);
        themePref = new ThemePref(this);

        btnAllowPermission = findViewById(R.id.btn_allow_permission);
        btnLater = findViewById(R.id.btn_later);
        txtPermissionMessage = findViewById(R.id.txt_permission_message);
        txtAppVersion = findViewById(R.id.txt_app_version);
        txtPrivacyPolicy = findViewById(R.id.txt_privacy_policy);
        scrollView = findViewById(R.id.scroll_view);

        btnAllowPermission.setOnClickListener(v -> Tools.requestPermission(ActivityPermission.this));
        btnLater.setOnClickListener(v -> finish());
        txtAppVersion.setText(getString(R.string.sub_about_app_version) + " " + BuildConfig.VERSION_NAME);
        txtPermissionMessage.setText(Html.fromHtml(getString(R.string.permission_message)));
        txtPrivacyPolicy.setOnClickListener(v -> new AlertDialog.Builder(ActivityPermission.this)
                .setTitle(getString(R.string.title_about_privacy))
                .setMessage(Html.fromHtml(sharedPref.getPrivacyPolicy()))
                .setPositiveButton(getString(R.string.option_ok), null)
                .show());

        if (themePref.getCurrentTheme() == THEME_DARK) {
            scrollView.setBackgroundResource(R.drawable.bg_rounded_corner_dark);
        } else {
            scrollView.setBackgroundResource(R.drawable.bg_rounded_corner);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finishAffinity();
            }
        }
    }

}
