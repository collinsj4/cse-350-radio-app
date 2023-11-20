package com.app.vaporwave.fragments;

import static com.app.vaporwave.utils.Constant.THEME_DARK;
import static com.app.vaporwave.utils.Constant.THEME_LIGHT;
import static com.app.vaporwave.utils.Constant.THEME_PRIMARY;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.app.vaporwave.BuildConfig;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.ActivityPermission;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.adapters.AdapterSearch;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.utils.Constant;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;

public class FragmentSettings extends DialogFragment {

    private Toolbar toolbar;
    private LinearLayout parentView;
    private ImageButton btnBack;
    private TextView toolbarTitle;
    RelativeLayout btnSwitchTheme;
    TextView txtTheme;
    private String singleChoiceSelected;
    ImageView btn_clear_cache;
    TextView txt_cache_size;
    private View rootView;
    private MainActivity activity;
    ThemePref themePref;
    SharedPref sharedPref;
    LinearLayout btnPermission;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        themePref = new ThemePref(activity);
        sharedPref = new SharedPref(activity);
        initView();
        setupToolbar();
        return rootView;
    }

    private void initView() {

        parentView = rootView.findViewById(R.id.parent_view);
        toolbar = rootView.findViewById(R.id.toolbar);
        toolbarTitle = rootView.findViewById(R.id.toolbar_title);
        btnBack = rootView.findViewById(R.id.btn_back);
        btnPermission = rootView.findViewById(R.id.btn_permission);

        btnSwitchTheme = rootView.findViewById(R.id.btn_switch_theme);
        txtTheme = rootView.findViewById(R.id.txt_theme);
        if (themePref.getCurrentTheme().equals(THEME_LIGHT)) {
            txtTheme.setText(getString(R.string.theme_light));
        } else if (themePref.getCurrentTheme().equals(THEME_DARK)) {
            txtTheme.setText(getString(R.string.theme_dark));
        } else {
            txtTheme.setText(getString(R.string.theme_primary));
        }

        btnSwitchTheme.setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_set_theme);
            singleChoiceSelected = items[themePref.getCurrentTheme()];
            int itemSelected = themePref.getCurrentTheme();
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.dialog_title_theme)
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.option_ok, (dialogInterface, i) -> {
                        if (singleChoiceSelected.equals(getString(R.string.theme_light))) {
                            themePref.updateTheme(THEME_LIGHT);
                            sharedPref.setIsDarkTheme(false);
                            txtTheme.setText(getString(R.string.theme_light));
                        } else if (singleChoiceSelected.equals(getString(R.string.theme_dark))) {
                            themePref.updateTheme(THEME_DARK);
                            sharedPref.setIsDarkTheme(true);
                            txtTheme.setText(getString(R.string.theme_dark));
                        } else if (singleChoiceSelected.equals(getString(R.string.theme_primary))) {
                            themePref.updateTheme(THEME_PRIMARY);
                            sharedPref.setIsDarkTheme(false);
                            txtTheme.setText(getString(R.string.theme_primary));
                        }
                        activity.recreate();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        rootView.findViewById(R.id.btn_notification).setOnClickListener(v -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID);
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
                intent.putExtra("app_uid", activity.getApplicationInfo().uid);
            }
            startActivity(intent);
        });

        txt_cache_size = rootView.findViewById(R.id.txt_cache_size);
        initializeCache();

        btn_clear_cache = rootView.findViewById(R.id.btn_clear_cache);
        btn_clear_cache.setOnClickListener(view -> clearCache());

        rootView.findViewById(R.id.lyt_clear_cache).setOnClickListener(v -> clearCache());

        rootView.findViewById(R.id.btn_clear_search_history).setOnClickListener(view -> {
            AdapterSearch adapterSearch = new AdapterSearch(activity);
            if (adapterSearch.getItemCount() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(getString(R.string.title_dialog_clear_search_history));
                builder.setMessage(getString(R.string.msg_dialog_clear_search_history));
                builder.setPositiveButton(R.string.option_yes, (di, i) -> {
                    ProgressDialog progressDialog = new ProgressDialog(activity);
                    progressDialog.setTitle(getResources().getString(R.string.msg_please_wait));
                    progressDialog.setMessage(getResources().getString(R.string.clearing_process));
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    adapterSearch.clearSearchHistory();
                    new Handler().postDelayed(() -> {
                        progressDialog.dismiss();
                        Snackbar.make(parentView, getString(R.string.clearing_success), Snackbar.LENGTH_SHORT).show();
                    }, 2000);
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            } else {
                Snackbar.make(parentView, getString(R.string.clearing_empty), Snackbar.LENGTH_SHORT).show();
            }
        });

        rootView.findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.title_about_privacy))
                .setMessage(Html.fromHtml(sharedPref.getPrivacyPolicy()))
                .setPositiveButton(getString(R.string.option_ok), null)
                .show()
        );

        rootView.findViewById(R.id.btn_rate).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))));

        rootView.findViewById(R.id.btn_share).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
        });

        rootView.findViewById(R.id.btn_more).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMoreAppsUrl()))));

        rootView.findViewById(R.id.btn_about).setOnClickListener(v -> {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.custom_dialog_about, null);
            TextView txtAppVersion = view.findViewById(R.id.txt_app_version);
            txtAppVersion.setText(getString(R.string.msg_about_version) + " " + BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")");
            final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setView(view);
            alert.setPositiveButton(R.string.option_ok, (dialog, which) -> dialog.dismiss());
            alert.show();
        });

        btnPermission.setOnClickListener(view -> startActivity(new Intent(activity, ActivityPermission.class)));
        permissionVisibility();

    }

    public void permissionVisibility() {
        if ((ContextCompat.checkSelfPermission(activity, "android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED)) {
            btnPermission.setVisibility(View.GONE);
        } else {
            btnPermission.setVisibility(View.VISIBLE);
        }
    }

    private void clearCache() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.msg_clear_cache);
        dialog.setPositiveButton(R.string.option_yes, (dialogInterface, i) -> {

            FileUtils.deleteQuietly(getActivity().getCacheDir());
            FileUtils.deleteQuietly(getActivity().getExternalCacheDir());

            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(R.string.msg_clearing_cache);
            progressDialog.setMessage(getString(R.string.msg_please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                txt_cache_size.setText(getString(R.string.sub_setting_clear_cache_start) + " 0 Bytes " + getString(R.string.sub_setting_clear_cache_end));
                Snackbar.make(activity.findViewById(android.R.id.content), getString(R.string.msg_cache_cleared), Snackbar.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }, 2000);

        });
        dialog.setNegativeButton(R.string.option_cancel, null);
        dialog.show();
    }

    private void initializeCache() {
        txt_cache_size.setText(getString(R.string.sub_setting_clear_cache_start) + " " + readableFileSize((0 + getDirSize(getActivity().getCacheDir())) + getDirSize(getActivity().getExternalCacheDir())) + " " + getString(R.string.sub_setting_clear_cache_end));
    }

    @SuppressWarnings("ConstantConditions")
    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, (double) digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }

    private void setupToolbar() {
        toolbarTitle.setText(getString(R.string.title_settings));
        btnBack.setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FragmentManager fm = activity.getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            }
            dismiss();
        }, Constant.DELAY_CLICK));
        themeColor();
    }

    private void themeColor() {
        if (themePref.getCurrentTheme() == THEME_LIGHT) {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.grey));
            btnBack.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        } else if (themePref.getCurrentTheme() == THEME_DARK) {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorBackgroundDark));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorToolbarDark));
            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.white));
        } else {
            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.white));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        permissionVisibility();
    }

}
