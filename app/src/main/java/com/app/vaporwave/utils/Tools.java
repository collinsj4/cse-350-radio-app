package com.app.vaporwave.utils;

import static com.app.vaporwave.utils.Constant.PERMISSIONS_REQUEST;
import static com.app.vaporwave.utils.Constant.THEME_DARK;
import static com.app.vaporwave.utils.Constant.THEME_LIGHT;
import static com.app.vaporwave.utils.Constant.THEME_PRIMARY;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.app.vaporwave.BuildConfig;
import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.ActivityPermission;
import com.app.vaporwave.activities.ActivityWebView;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.database.dao.AppDatabase;
import com.app.vaporwave.database.dao.DAO;
import com.app.vaporwave.database.dao.RadioEntity;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.models.Radio;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {

    private Context context;
    private ThemePref themePref;
    private SharedPref sharedPref;
    private BottomSheetDialog mBottomSheetDialog;
    private boolean flagReadLater;
    private DAO db;

    public Tools(Context context) {
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.themePref = new ThemePref(context);
        this.db = AppDatabase.getDb(context).get();
    }

    public void showBottomSheetDialog(Activity activity, View parentView, Radio radio) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.include_bottom_sheet, null);

        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);

        ImageView radioImage = view.findViewById(R.id.sheet_radio_image);
        TextView radioName = view.findViewById(R.id.sheet_radio_name);
        TextView radioCategory = view.findViewById(R.id.sheet_category_name);

        TextView txtFavorite = view.findViewById(R.id.txt_favorite);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgShare = view.findViewById(R.id.img_share);
        ImageView imgReport = view.findViewById(R.id.img_report);

        Glide.with(context)
                .load(sharedPref.getBaseUrl() + "/upload/" + radio.radio_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(radioImage);

        radioName.setText(radio.radio_name);
        radioCategory.setText(radio.category_name);

        if (themePref.getCurrentTheme().equals(THEME_DARK)) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.white));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.white));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.white));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.grey_dark));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);
        LinearLayout btnReport = view.findViewById(R.id.btn_report);

        btnFavorite.setOnClickListener(v -> {
            if (Tools.isNetworkAvailable(activity)) {
                flagReadLater = db.getRadio(radio.radio_id) != null;
                if (flagReadLater) {
                    db.deleteRadio(radio.radio_id);
                    imgFavorite.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_star_outline));
                    Snackbar.make(parentView, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                } else {
                    db.insertRadio(RadioEntity.entity(radio));
                    imgFavorite.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_star_white));
                    Snackbar.make(parentView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            if (Constant.item_radio.size() > 0) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_radio_text) + " - " + Constant.item_radio.get(Constant.position).radio_name + "\n" + activity.getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                activity.startActivity(share);
            }
            mBottomSheetDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            String str;
            try {
                str = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{activity.getString(R.string.report_email)});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Report " + radio.radio_name + " channel issue in " + activity.getResources().getString(R.string.app_name));
                intent.putExtra(Intent.EXTRA_TEXT, "Device OS : Android \n Device OS version : " +
                        Build.VERSION.RELEASE + "\n App Version : " + str + "\n Device Brand : " + Build.BRAND +
                        "\n Device Model : " + Build.MODEL + "\n Device Manufacturer : " + Build.MANUFACTURER + "\n" + "Message : ");
                try {
                    activity.startActivity(Intent.createChooser(intent, activity.getResources().getString(R.string.menu_report)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity.getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            mBottomSheetDialog.dismiss();
        });

        if (themePref.getCurrentTheme().equals(THEME_DARK)) {
            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
        } else {
            this.mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

        flagReadLater = db.getRadio(radio.radio_id) != null;
        if (flagReadLater) {
            txtFavorite.setText(activity.getString(R.string.favorite_remove));
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite);
        } else {
            txtFavorite.setText(activity.getString(R.string.favorite_add));
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        }

    }

    public static void requestPermission(Activity activity) {
        if ((ContextCompat.checkSelfPermission(activity, "android.permission.READ_PHONE_STATE") != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, PERMISSIONS_REQUEST);
            }
        }
    }

    public static void checkPermission(Activity activity) {
        if ((ContextCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(activity, "android.permission.READ_PHONE_STATE") != PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.startActivity(new Intent(activity, ActivityPermission.class));
                activity.finish();
            }
        }
    }

    public static AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public static void notificationOpenHandler(Context context, Intent getIntent) {
        long unique_id = getIntent.getLongExtra("unique_id", 0);
        long post_id = getIntent.getLongExtra("post_id", 0);
        String title = getIntent.getStringExtra("title");
        String link = getIntent.getStringExtra("link");
        if (post_id == 0) {
            if (link != null && !link.equals("")) {
                if (Config.OPEN_NOTIFICATION_LINK_IN_EXTERNAL_BROWSER) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                } else {
                    Intent intent = new Intent(context, ActivityWebView.class);
                    intent.putExtra("title", title);
                    intent.putExtra("url", link);
                    context.startActivity(intent);
                }
            }
        } else {
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }
        Log.d("push_notification", "unique id : " + unique_id);
        Log.d("push_notification", "link : " + link);
        Log.d("push_notification", "post id : " + post_id);
    }

    public static void getTheme(Context context) {
        ThemePref themePref = new ThemePref(context);
        if (themePref.getCurrentTheme() == THEME_LIGHT) {
            context.setTheme(R.style.AppLightTheme);
        } else if (themePref.getCurrentTheme() == THEME_DARK) {
            context.setTheme(R.style.AppDarkTheme);
        } else if (themePref.getCurrentTheme() == THEME_PRIMARY) {
            context.setTheme(R.style.AppPrimaryTheme);
        } else {
            context.setTheme(R.style.AppPrimaryTheme);
        }
    }

    public static void darkNavigation(Activity activity) {
        activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundDark));
        activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundDark));
        activity.getWindow().getDecorView().setSystemUiVisibility(0);
    }

    public static void primaryNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundLight));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundLight));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundLight));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            //activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void primaryStatusBar(Activity activity, boolean flag) {
        View view = activity.getWindow().getDecorView();
        if (flag) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundLight));
                view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
            primaryNavigation(activity);
        }
    }

    public static void lightStatusBar(Activity activity, boolean flag) {
        View view = activity.getWindow().getDecorView();
        if (flag) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundLight));
                activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundLight));
                view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorBackgroundLight));
            lightNavigation(activity);
        }
    }

    public static void setStatusBarColor(Activity activity, int color) {
        activity.getWindow().setStatusBarColor(color);
    }

    public static String decrypt(String code) {
        return decodeBase64(decodeBase64(code));
    }

    public static String decodeBase64(String code) {
        byte[] valueDecoded = Base64.decode(code.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return new String(valueDecoded);
    }

    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String hourString = "";
        String secondsString = "";
        String minutesString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        long temp_milli = Constant.exoPlayer.getDuration();
        int temp_hour = (int) (temp_milli / (1000 * 60 * 60));
        if (temp_hour != 0) {
            hourString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        // Prepending 0 to minutes if it is one digit
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        finalTimerString = hourString + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    public static long getSeekFromPercentage(int percentage, long totalDuration) {

        long currentSeconds = 0;
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        currentSeconds = (percentage * totalSeconds) / 100;

        // return percentage
        return currentSeconds * 1000;
    }

    public static int calculateTime(String duration) {
        int time = 0, min, sec, hr = 0;
        try {
            StringTokenizer st = new StringTokenizer(duration, ".");
            if (st.countTokens() == 3) {
                hr = Integer.parseInt(st.nextToken());
            }
            min = Integer.parseInt(st.nextToken());
            sec = Integer.parseInt(st.nextToken());
        } catch (Exception e) {
            StringTokenizer st = new StringTokenizer(duration, ":");
            if (st.countTokens() == 3) {
                hr = Integer.parseInt(st.nextToken());
            }
            min = Integer.parseInt(st.nextToken());
            sec = Integer.parseInt(st.nextToken());
        }
        time = ((hr * 3600) + (min * 60) + sec) * 1000;
        return time;
    }

    public long convertToMilliSeconds(String s) {

        long ms = 0;
        Pattern p;
        if (s.contains(("\\:"))) {
            p = Pattern.compile("(\\d+):(\\d+)");
        } else {
            p = Pattern.compile("(\\d+).(\\d+)");
        }
        Matcher m = p.matcher(s);
        if (m.matches()) {
            int h = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            // int sec = Integer.parseInt(m.group(2));
            ms = (long) h * 60 * 60 * 1000 + min * 60 * 1000;
        }
        return ms;
    }

    public static void getPosition(Boolean isNext) {
        if (isNext) {
            if (Constant.position != Constant.item_radio.size() - 1) {
                Constant.position = Constant.position + 1;
            } else {
                Constant.position = 0;
            }
        } else {
            if (Constant.position != 0) {
                Constant.position = Constant.position - 1;
            } else {
                Constant.position = Constant.item_radio.size() - 1;
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isConnected() || activeNetworkInfo.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

//    public void clickListener(final int pos, final String type) {
//        onItemClickListener.onClick(pos, type);
//    }

    public interface onItemClickListener {
        void onClick(int position, String type);
    }

    public class IMMResult extends ResultReceiver {

        public int result = -1;

        public IMMResult() {
            super(null);
        }

        @Override
        public void onReceiveResult(int r, Bundle data) {
            result = r;
        }

        // poll result value for up to 500 milliseconds
        public int getResult() {
            try {
                int sleep = 0;
                while (result == -1 && sleep < 500) {
                    Thread.sleep(100);
                    sleep += 100;
                }
            } catch (InterruptedException e) {
                Log.e("IMMResult", e.getMessage());
            }
            return result;
        }
    }

    public static int getScreenWidth(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    //Get response from an URL request (GET)
    public static String getDataFromUrl(String url) {
        // Making HTTP request
        Log.v("INFO", "Requesting: " + url);

        StringBuffer chaine = new StringBuffer("");
        try {
            URL urlCon = new URL(url);

            //Open a connection
            HttpURLConnection connection = (HttpURLConnection) urlCon
                    .openConnection();
            connection.setRequestProperty("User-Agent", "Your Single Radio");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            //Handle redirecti
            int status = connection.getResponseCode();
            if ((status != HttpURLConnection.HTTP_OK)
                    && (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)) {

                // get redirect url from "location" header field
                String newUrl = connection.getHeaderField("Location");
                // get the cookie if need, for login
                String cookies = connection.getHeaderField("Set-Cookie");

                // open the new connnection again
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                connection.setRequestProperty("Cookie", cookies);
                connection.setRequestProperty("User-Agent", "Your Single Radio");
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                System.out.println("Redirect to URL : " + newUrl);
            }

            //Get the stream from the connection and read it
            InputStream inputStream = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
        }

        return chaine.toString();
    }

    //Get JSON from an url and parse it to a JSON Object.
    public static JSONObject getJSONObjectFromUrl(String url) {
        String data = getDataFromUrl(url);

        try {
            return new JSONObject(data);
        } catch (Exception e) {
            Log.e("INFO", "Error parsing JSON. Printing stacktrace now");
        }

        return null;
    }

    public static String getUserAgent() {

        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version"));
        result.append(" (Linux; U; Android ");

        String version = Build.VERSION.RELEASE;
        result.append(version.length() > 0 ? version : "1.0");

        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                result.append("; ");
                result.append(model);
            }
        }

        String id = Build.ID;

        if (id.length() > 0) {
            result.append(" Build/");
            result.append(id);
        }

        result.append(")");
        return result.toString();
    }

    public static Bitmap blurImage(Activity activity, Bitmap bitmap) {
        try {
            RenderScript rsScript = RenderScript.create(activity);
            Allocation allocation = Allocation.createFromBitmap(rsScript, bitmap);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));
            blur.setRadius(10);
            blur.setInput(allocation);

            Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        } catch (Exception e) {
            return bitmap;
        }

    }

}