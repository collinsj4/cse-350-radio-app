package com.app.vaporwave.activities;

import static com.app.vaporwave.utils.Constant.THEME_DARK;
import static com.app.vaporwave.utils.Constant.THEME_LIGHT;
import static com.app.vaporwave.utils.Constant.THEME_PRIMARY;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;

import com.app.vaporwave.BuildConfig;
import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.database.dao.AppDatabase;
import com.app.vaporwave.database.dao.DAO;
import com.app.vaporwave.database.dao.RadioEntity;
import com.app.vaporwave.database.prefs.AdsPref;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.fragments.FragmentCategoryDetail;
import com.app.vaporwave.fragments.FragmentSearch;
import com.app.vaporwave.fragments.FragmentSettings;
import com.app.vaporwave.models.Radio;
import com.app.vaporwave.services.RadioPlayerService;
import com.app.vaporwave.utils.AdsManager;
import com.app.vaporwave.utils.AppBarLayoutBehavior;
import com.app.vaporwave.utils.Constant;
import com.app.vaporwave.utils.PaletteUtils;
import com.app.vaporwave.utils.RelativePopupWindow;
import com.app.vaporwave.utils.RtlViewPager;
import com.app.vaporwave.utils.SleepTimeReceiver;
import com.app.vaporwave.utils.Tools;
import com.app.vaporwave.utils.ViewPagerHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import es.claucookie.miniequalizerlibrary.EqualizerView;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    SlidingUpPanelLayout slidingUpPanelLayout;
    ProgressBar progressBar, progressBarCollapse;
    SeekBar seekBarSong;
    LinearLayout lytCollapseColor, lytPlayerExpand, lytPlayCollapse;
    LinearLayout lytCollapse;
    RelativeLayout lytExpand, lytSeekBar;
    ImageView imgRadioSmall;
    ImageView imgRadioLarge;
    ImageView imgAlbumArtSmall;
    ImageView imgAlbumArtLarge;
    ImageView imgMusicBackground;
    ImageButton imgNextExpand, imgPreviousExpand, imgVolume, imgFavorite;
    ImageButton imgCollapse, imgTimer, imgShare;
    ImageButton imgPlay, imgNext, imgPrevious;
    MaterialButton fabPlayExpand;
    TextView txtName, txtSong, txtRadioExpand, txtSongExpand, txtDuration, txtTotalDuration;
    Boolean isExpand = false;
    SharedPref sharedPref;
    Handler seekHandler = new Handler();
    Handler handler = new Handler();
    Toolbar toolbar;
    FragmentManager fragmentManager;
    EqualizerView equalizerView;
    View lytMusicScreen;
    Tools tools;
    AdsPref adsPref;
    ThemePref themePref;
    private DAO db;
    private boolean flagReadLater;
    AppBarLayout appBarLayout;
    AdsManager adsManager;
    InterstitialAd adMobInterstitialAd;
    int counter = 1;
    private AppUpdateManager appUpdateManager;
    boolean isShowingInterstitialAd = false;
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    ViewPagerHelper viewPagerHelper;
    CoordinatorLayout parentView;
    BottomNavigationView bottomNavigationView;
    private BottomSheetDialog mBottomSheetDialog;
    View bgLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        adsPref = new AdsPref(this);
        setContentView(R.layout.activity_main);
        db = AppDatabase.getDb(this).get();
        themePref = new ThemePref(this);

        if (Config.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        adsManager = new AdsManager(this);
        adsManager.initializeAd();
        adsManager.updateConsentStatus();
        adsManager.loadBannerAd(Config.BANNER_HOME);
        adsManager.loadInterstitialAd(Config.INTERSTITIAL_CATEGORY_LIST, adsPref.getInterstitialAdInterval());
        loadAdMobInterstitialAd();

        Constant.is_app_open = true;

        sharedPref = new SharedPref(this);
        sharedPref.setCheckSleepTime();
        tools = new Tools(this);

        setupToolbar();
        initComponent();
        setupBottomNavigation();
        setupViewPager();

        themeColor();
        Tools.notificationOpenHandler(this, getIntent());

        if (!BuildConfig.DEBUG) {
            appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
            inAppUpdate();
            inAppReview();
        }

    }

    public void showInterstitialAd() {
        adsManager.showInterstitialAd();
    }

    public void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    public void initComponent() {

        fragmentManager = getSupportFragmentManager();
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        lytMusicScreen = findViewById(R.id.layout_music_player);
        imgMusicBackground = findViewById(R.id.img_music_background);

        equalizerView = findViewById(R.id.equalizer_view);
        progressBar = findViewById(R.id.progress_bar);
        progressBarCollapse = findViewById(R.id.progress_bar_collapse);
        seekBarSong = findViewById(R.id.seek_bar_song);
        imgShare = findViewById(R.id.img_share);
        imgTimer = findViewById(R.id.img_timer);
        imgRadioSmall = findViewById(R.id.img_radio_small);
        imgRadioLarge = findViewById(R.id.img_radio_large);
        imgAlbumArtSmall = findViewById(R.id.img_album_art_small);
        imgAlbumArtLarge = findViewById(R.id.img_album_art_large);
        imgPrevious = findViewById(R.id.img_player_previous);
        imgPreviousExpand = findViewById(R.id.img_previous_expand);
        imgNext = findViewById(R.id.img_player_next);
        imgNextExpand = findViewById(R.id.img_next_expand);
        imgPlay = findViewById(R.id.img_player_play);
        imgFavorite = findViewById(R.id.img_favorite);
        imgVolume = findViewById(R.id.img_volume);
        imgCollapse = findViewById(R.id.img_collapse);
        txtName = findViewById(R.id.txt_radio_name);

        txtSong = findViewById(R.id.txt_metadata);
        txtSong.setSelected(true);

        fabPlayExpand = findViewById(R.id.fab_play);
        txtRadioExpand = findViewById(R.id.txt_radio_name_expand);

        txtSongExpand = findViewById(R.id.txt_metadata_expand);
        txtSongExpand.setSelected(true);

        txtDuration = findViewById(R.id.txt_song_duration);
        txtTotalDuration = findViewById(R.id.txt_total_duration);

        lytPlayerExpand = findViewById(R.id.lyt_player_expand);
        lytPlayCollapse = findViewById(R.id.lyt_play_collapse);
        lytSeekBar = findViewById(R.id.lyt_song_seek_bar);
        lytCollapse = findViewById(R.id.lyt_collapse);
        lytCollapseColor = findViewById(R.id.lyt_collapse_color);
        lytExpand = findViewById(R.id.ll_expand);

        bgLine = findViewById(R.id.bg_line);
        parentView = findViewById(R.id.coordinator_layout);

        if (!Tools.isNetworkAvailable(this)) {
            txtName.setText(getResources().getString(R.string.app_name));
            txtRadioExpand.setText(getResources().getString(R.string.app_name));
            txtSong.setText(getResources().getString(R.string.internet_not_connected));
            txtSongExpand.setText(getResources().getString(R.string.internet_not_connected));
            lytSeekBar.setVisibility(View.GONE);
        }

        setIfPlaying();

        seekBarSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                try {
                    Constant.exoPlayer.seekTo((int) Tools.getSeekFromPercentage(progress, Constant.exoPlayer.getDuration()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        lytCollapse.setOnClickListener(v -> slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED));

        lytExpand.setOnClickListener(v -> {

        });

        slidingUpPanelLayout.setDragView(lytCollapse);

        slidingUpPanelLayout.setShadowHeight(0);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0f) {
                    isExpand = false;
                    lytExpand.setVisibility(View.GONE);
                } else if (slideOffset > 0.0f && slideOffset < 1.0f) {
                    if (isExpand) {
                        lytCollapse.setVisibility(View.VISIBLE);
                        bottomNavigationView.setVisibility(View.VISIBLE);
                        lytExpand.setAlpha(slideOffset);
                    } else {
                        lytExpand.setVisibility(View.VISIBLE);
                        lytExpand.setAlpha(0.0f + slideOffset);
                    }
                    lytCollapse.setAlpha(1.0f - slideOffset);
                    bottomNavigationView.setAlpha(1.0f - slideOffset);
                } else {
                    isExpand = true;
                    lytCollapse.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    lytExpand.setVisibility(View.VISIBLE);
                    lytCollapse.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.GONE);
                    changePlayerBackground(Constant.colorBitmap, true);
                    Constant.isPlayerExpanded = true;

                    if (Constant.item_radio.size() > 0) {
                        changeFav(Constant.item_radio.get(Constant.position));
                        if (themePref.getCurrentTheme().equals(THEME_LIGHT)) {
                            Tools.lightStatusBar(MainActivity.this, false);
                        }
                    }

                    Log.d(TAG, "expanded");
                }
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    lytExpand.setVisibility(View.GONE);
                    lytCollapse.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    if (themePref.getCurrentTheme() == THEME_DARK) {
                        Tools.darkNavigation(MainActivity.this);
                    } else if (themePref.getCurrentTheme() == THEME_LIGHT) {
                        Tools.lightStatusBar(MainActivity.this, true);
                    } else if (themePref.getCurrentTheme() == THEME_PRIMARY) {
                        Tools.primaryStatusBar(MainActivity.this, false);
                    }
                    Constant.isPlayerExpanded = false;
                    Log.d(TAG, "collapsed");
                }
            }
        });

        imgPlay.setOnClickListener(view -> {
            if (!Tools.isNetworkAvailable(this)) {
                showSnackBar(getString(R.string.internet_not_connected));
            } else {
                clickPlay(Constant.position);
            }
        });

        fabPlayExpand.setOnClickListener(view -> {
            if (!Tools.isNetworkAvailable(this)) {
                showSnackBar(getString(R.string.internet_not_connected));
            } else {
                clickPlay(Constant.position);
            }
        });

        imgNext.setOnClickListener(view -> togglePlayPosition(true));
        imgNextExpand.setOnClickListener(view -> togglePlayPosition(true));

        imgPrevious.setOnClickListener(view -> togglePlayPosition(false));
        imgPreviousExpand.setOnClickListener(view -> togglePlayPosition(false));

        imgTimer.setOnClickListener(v -> {
            if (sharedPref.getIsSleepTimeOn()) {
                openTimeDialog();
            } else {
                openTimeSelectDialog();
            }
        });

        imgShare.setOnClickListener(v -> {
            if (Constant.item_radio.size() > 0) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_radio_text) + " - " + Constant.item_radio.get(Constant.position).radio_name + "\n" + getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(share);
            }
        });

        imgCollapse.setOnClickListener(v -> slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));

        imgVolume.setOnClickListener(v -> changeVolume());

        imgFavorite.setOnClickListener(view -> {
            if (Tools.isNetworkAvailable(this)) {
                Radio radio = Constant.item_radio.get(Constant.position);
                String radio_id = Constant.item_radio.get(Constant.position).radio_id;
                flagReadLater = db.getRadio(radio_id) != null;
                if (flagReadLater) {
                    db.deleteRadio(radio_id);
                    imgFavorite.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_star_outline));
                    showSnackBar(getString(R.string.favorite_removed));
                } else {
                    db.insertRadio(RadioEntity.entity(radio));
                    imgFavorite.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_star_white));
                    showSnackBar(getString(R.string.favorite_added));
                }
            }
        });

    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        if (themePref.getCurrentTheme() == THEME_DARK) {
            int[] colors = new int[]{
                    ContextCompat.getColor(getApplicationContext(), R.color.navigationInactive),
                    ContextCompat.getColor(getApplicationContext(), R.color.navigationActive)
            };
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_enabled, -android.R.attr.state_checked},
                    new int[]{android.R.attr.state_enabled, android.R.attr.state_checked}
            };
            bottomNavigationView.setItemTextColor(new ColorStateList(states, colors));
            bottomNavigationView.setItemIconTintList(new ColorStateList(states, colors));
            bottomNavigationView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarDark));
        }
    }

    private void setupViewPager() {
        bottomNavigationView.getMenu().clear();

        viewPagerHelper = new ViewPagerHelper(this);
        viewPager = findViewById(R.id.view_pager);
        viewPagerRTL = findViewById(R.id.view_pager_rtl);

        if (Config.ENABLE_RTL_MODE) {
            if (Config.SIMPLE_MODE) {
                bottomNavigationView.inflateMenu(R.menu.menu_navigation_simple);
                viewPagerHelper.setupViewPagerSimpleRTL(fragmentManager, viewPagerRTL, bottomNavigationView, toolbar);
            } else {
                bottomNavigationView.inflateMenu(R.menu.menu_navigation);
                viewPagerHelper.setupViewPagerRTL(fragmentManager, viewPagerRTL, bottomNavigationView, toolbar);
            }
            viewPager.setVisibility(View.GONE);
            viewPagerRTL.setVisibility(View.VISIBLE);
        } else {
            if (Config.SIMPLE_MODE) {
                bottomNavigationView.inflateMenu(R.menu.menu_navigation_simple);
                viewPagerHelper.setupViewPagerSimple(fragmentManager, viewPager, bottomNavigationView, toolbar);
            } else {
                bottomNavigationView.inflateMenu(R.menu.menu_navigation);
                viewPagerHelper.setupViewPager(fragmentManager, viewPager, bottomNavigationView, toolbar);
            }
            viewPager.setVisibility(View.VISIBLE);
            viewPagerRTL.setVisibility(View.GONE);
        }
    }

    public void loadFrag(Fragment f1, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(android.R.id.content, f1);
        ft.commit();
    }

    private void togglePlayPosition(Boolean isNext) {
        if (Constant.item_radio.size() > 0) {
            if (RadioPlayerService.getInstance() == null) {
                RadioPlayerService.createInstance().initializeRadio(MainActivity.this, Constant.item_radio.get(Constant.position));
            }
            Intent intent;
            intent = new Intent(MainActivity.this, RadioPlayerService.class);
            if (isNext) {
                intent.setAction(RadioPlayerService.ACTION_NEXT);
            } else {
                intent.setAction(RadioPlayerService.ACTION_PREVIOUS);
            }
            startService(intent);
        }
    }

    public void clickPlay(int position) {
        Constant.position = position;
        if (Constant.item_radio != null && Constant.item_radio.size() > 0) {
            Radio radio = Constant.item_radio.get(Constant.position);

            final Intent intent = new Intent(MainActivity.this, RadioPlayerService.class);

            if (RadioPlayerService.getInstance() != null) {
                Radio playerCurrentRadio = RadioPlayerService.getInstance().getPlayingRadioStation();
                if (playerCurrentRadio != null) {
                    if (!radio.radio_id.equals(RadioPlayerService.getInstance().getPlayingRadioStation().radio_id)) {
                        RadioPlayerService.getInstance().initializeRadio(MainActivity.this, radio);
                        intent.setAction(RadioPlayerService.ACTION_PLAY);
                    } else {
                        intent.setAction(RadioPlayerService.ACTION_TOGGLE);
                    }
                } else {
                    RadioPlayerService.getInstance().initializeRadio(MainActivity.this, radio);
                    intent.setAction(RadioPlayerService.ACTION_PLAY);
                }
            } else {
                RadioPlayerService.createInstance().initializeRadio(MainActivity.this, radio);
                intent.setAction(RadioPlayerService.ACTION_PLAY);
            }
            startService(intent);
        }
    }

    public void changePlayPause(Boolean flag) {
        Constant.is_playing = flag;
        if (flag) {
            Radio radio = RadioPlayerService.getInstance().getPlayingRadioStation();
            if (radio != null) {
                changeText(radio);
                changeFav(radio);
                imgPlay.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_pause_white));
                fabPlayExpand.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_pause_white));
                equalizerView.animateBars();
            }
        } else {
            if (Constant.item_radio.size() > 0) {
                changeText(Constant.item_radio.get(Constant.position));
                changeFav(Constant.item_radio.get(Constant.position));
            }
            imgPlay.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_play_arrow_white));
            fabPlayExpand.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_play_arrow_white));
            equalizerView.stopBars();
            imgAlbumArtSmall.setVisibility(View.GONE);
            imgAlbumArtLarge.setVisibility(View.GONE);
        }
    }

    public void changeFav(Radio radio) {
        checkFav(radio.radio_id);
    }

    private void checkFav(String id) {
        flagReadLater = db.getRadio(id) != null;
        if (flagReadLater) {
            imgFavorite.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_star_white));
        } else {
            imgFavorite.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_star_outline));
        }
    }

    public void showImageAlbumArt(boolean show) {
        if (show) {
            imgAlbumArtSmall.setVisibility(View.VISIBLE);
            imgAlbumArtLarge.setVisibility(View.VISIBLE);
        } else {
            imgAlbumArtSmall.setVisibility(View.GONE);
            imgAlbumArtLarge.setVisibility(View.GONE);
        }
    }

    public void changeText(Radio radio) {
        if (Constant.radio_type) {
            changeSongName(Constant.metadata);
            if (Constant.metadata == null || Constant.metadata.equals(radio.category_name)) {
                imgAlbumArtSmall.setVisibility(View.GONE);
                imgAlbumArtLarge.setVisibility(View.GONE);
            } else {
                imgAlbumArtSmall.setVisibility(View.VISIBLE);
                imgAlbumArtLarge.setVisibility(View.VISIBLE);
            }
            txtSongExpand.setVisibility(View.VISIBLE);
            lytSeekBar.setVisibility(View.GONE);
        } else {
            txtSong.setText(radio.category_name);
            txtSongExpand.setText(radio.category_name);
            txtSongExpand.setVisibility(View.VISIBLE);
            lytSeekBar.setVisibility(View.VISIBLE);
            showImageAlbumArt(false);
        }

        txtName.setText(radio.radio_name);
        txtRadioExpand.setText(radio.radio_name);

        if (!Constant.is_playing) {
            txtSong.setText(radio.category_name);
            txtSongExpand.setText(radio.category_name);
        }

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(sharedPref.getBaseUrl() + "/upload/" + radio.radio_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_artwork)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        imgRadioLarge.setImageBitmap(bitmap);
                        imgRadioSmall.setImageBitmap(bitmap);
                        showBlurBackgroundImage(bitmap);

                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        Bitmap newBitmap = PaletteUtils.getDominantGradient(bitmap, displayMetrics.heightPixels, displayMetrics.widthPixels);
                        changePlayerBackground(newBitmap, Constant.isPlayerExpanded);
                        Constant.colorBitmap = newBitmap;

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

    }

    public void showBlurBackgroundImage(Bitmap bitmap) {
        RelativeLayout lytBlurImage = findViewById(R.id.lyt_background_blur);
        if (Config.DISPLAY_RADIO_BACKGROUND_BLUR_IMAGE) {
            ImageView imgRadioBlur = findViewById(R.id.img_music_background_blur);
            Bitmap blurImage = Tools.blurImage(MainActivity.this, bitmap);
            imgRadioBlur.setImageBitmap(blurImage);
            lytBlurImage.setVisibility(View.VISIBLE);
        } else {
            lytBlurImage.setVisibility(View.GONE);
        }
    }

    public void changePlayerBackground(Bitmap bitmap, boolean changeStatusBarColor) {
        if (themePref.getCurrentTheme().equals(THEME_DARK)) {
            imgMusicBackground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundDark));
            if (changeStatusBarColor) {
                if (Config.DISPLAY_RADIO_BACKGROUND_BLUR_IMAGE) {
                    if (bitmap != null) {
                        Palette.from(bitmap).generate(palette -> {
                            int defaultColor = getResources().getColor(R.color.colorBackgroundDark);
                            assert palette != null;
                            int color = palette.getDominantColor(defaultColor);
                            int newColor = ColorUtils.blendARGB(color, Color.BLACK, 0.5f);
                            imgMusicBackground.setBackgroundColor(newColor);
                            Tools.setStatusBarColor(MainActivity.this, newColor);
                        });
                    }
                } else {
                    Tools.setStatusBarColor(MainActivity.this, ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundDark));
                }
            }
        } else {
            if (bitmap != null) {
                Palette.from(bitmap).generate(palette -> {
                    int defaultColor = getResources().getColor(R.color.colorPrimaryDark);
                    assert palette != null;
                    int color = palette.getDominantColor(defaultColor);
                    int newColor = ColorUtils.blendARGB(color, Color.BLACK, 0.5f);
                    imgMusicBackground.setBackgroundColor(newColor);
                    if (changeStatusBarColor) {
                        Tools.setStatusBarColor(MainActivity.this, newColor);
                    }
                });
            }
        }
    }

    public void changeSongName(String songName) {
        Constant.metadata = songName;
        txtSong.setText(songName);
        txtSongExpand.setText(songName);
    }

    public void changeAlbumArt(String artworkUrl) {
        Constant.albumArt = artworkUrl;

        Glide.with(getApplicationContext())
                .load(artworkUrl.replace(" ", "%20"))
                .placeholder(android.R.color.transparent)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        imgAlbumArtSmall.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        imgAlbumArtSmall.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imgAlbumArtSmall);

        Glide.with(getApplicationContext())
                .load(artworkUrl.replace(" ", "%20"))
                .placeholder(android.R.color.transparent)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        imgAlbumArtLarge.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        imgAlbumArtLarge.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imgAlbumArtLarge);
    }

    public void setIfPlaying() {
        if (RadioPlayerService.getInstance() != null) {
            RadioPlayerService.initialize(MainActivity.this);
            changePlayPause(RadioPlayerService.getInstance().isPlaying());
            seekBarUpdate();
        } else {
            changePlayPause(false);
        }
    }

    private final Runnable run = () -> {
        try {
            seekBarUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public void seekBarUpdate() {
        try {
            if (Constant.is_app_open) {
                seekBarSong.setProgress(Tools.getProgressPercentage(Constant.exoPlayer.getCurrentPosition(), Constant.exoPlayer.getDuration()));
                txtDuration.setText(Tools.milliSecondsToTimer(Constant.exoPlayer.getCurrentPosition()));
                txtTotalDuration.setText(Tools.milliSecondsToTimer(Constant.exoPlayer.getDuration()));
                seekBarSong.setSecondaryProgress(Constant.exoPlayer.getBufferedPercentage());
                if (RadioPlayerService.getInstance().isPlaying()) {
                    seekHandler.removeCallbacks(run);
                    seekHandler.postDelayed(run, 1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideSeekBar() {
        lytSeekBar.setVisibility(View.GONE);
    }

    public void setBuffer(Boolean flag) {
        if (flag) {
            Constant.progressVisibility = true;
            progressBar.setVisibility(View.VISIBLE);
            lytPlayerExpand.setVisibility(View.INVISIBLE);
            progressBarCollapse.setVisibility(View.VISIBLE);
            lytPlayCollapse.setVisibility(View.INVISIBLE);
        } else {
            Constant.progressVisibility = false;
            progressBar.setVisibility(View.INVISIBLE);
            lytPlayerExpand.setVisibility(View.VISIBLE);
            progressBarCollapse.setVisibility(View.INVISIBLE);
            lytPlayCollapse.setVisibility(View.VISIBLE);
        }
    }

    public void changeVolume() {
        final RelativePopupWindow popupWindow = new RelativePopupWindow(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.lyt_volume, null);
        ImageView imageView1 = view.findViewById(R.id.img_volume_max);
        ImageView imageView2 = view.findViewById(R.id.img_volume_min);
        imageView1.setColorFilter(Color.BLACK);
        imageView2.setColorFilter(Color.BLACK);

        VerticalSeekBar seekBar = view.findViewById(R.id.seek_bar_volume);
        seekBar.getThumb().setColorFilter(sharedPref.getFirstColor(), PorterDuff.Mode.SRC_IN);
        seekBar.getProgressDrawable().setColorFilter(sharedPref.getSecondColor(), PorterDuff.Mode.SRC_IN);

        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        seekBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekBar.setProgress(volume_level);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setContentView(view);
        popupWindow.showOnAnchor(imgVolume, RelativePopupWindow.VerticalPosition.ABOVE, RelativePopupWindow.HorizontalPosition.CENTER);
    }

    public void openTimeSelectDialog() {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setTitle(getString(R.string.sleep_time));

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.lyt_dialog_select_time, null);
        alt_bld.setView(dialogView);

        final TextView tv_min = dialogView.findViewById(R.id.txt_minutes);
        tv_min.setText("1 " + getString(R.string.min));

        SeekBar seekBar = dialogView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_min.setText(progress + " " + getString(R.string.min));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        alt_bld.setPositiveButton(getString(R.string.set), (dialog, which) -> {
            String hours = String.valueOf(seekBar.getProgress() / 60);
            String minute = String.valueOf(seekBar.getProgress() % 60);

            if (hours.length() == 1) {
                hours = "0" + hours;
            }

            if (minute.length() == 1) {
                minute = "0" + minute;
            }

            String totalTime = hours + ":" + minute;
            long total_timer = tools.convertToMilliSeconds(totalTime) + System.currentTimeMillis();

            Random random = new Random();
            int id = random.nextInt(100);

            sharedPref.setSleepTime(true, total_timer, id);

            int FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                FLAG = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT;
            } else {
                FLAG = PendingIntent.FLAG_ONE_SHOT;
            }

            Intent intent = new Intent(MainActivity.this, SleepTimeReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, intent, FLAG);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            assert alarmManager != null;
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, total_timer, pendingIntent);
        });
        alt_bld.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {

        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    public void openTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.sleep_time));
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.lyt_dialog_time, null);
        builder.setView(dialogView);

        TextView textView = dialogView.findViewById(R.id.txt_time);

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {

        });

        builder.setPositiveButton(getString(R.string.stop), (dialog, which) -> {
            int FLAG;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                FLAG = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT;
            } else {
                FLAG = PendingIntent.FLAG_ONE_SHOT;
            }
            Intent intent = new Intent(MainActivity.this, SleepTimeReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, sharedPref.getSleepID(), intent, FLAG);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            pendingIntent.cancel();
            assert alarmManager != null;
            alarmManager.cancel(pendingIntent);
            sharedPref.setSleepTime(false, 0, 0);
        });

        updateTimer(textView, sharedPref.getSleepTime());

        builder.show();
    }

    private void updateTimer(final TextView textView, long time) {
        long timeLeft = time - System.currentTimeMillis();
        if (timeLeft > 0) {
            @SuppressLint("DefaultLocale") String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeLeft),
                    TimeUnit.MILLISECONDS.toMinutes(timeLeft) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(timeLeft) % TimeUnit.MINUTES.toSeconds(1));

            textView.setText(hms);
            handler.postDelayed(() -> {
                if (sharedPref.getIsSleepTimeOn()) {
                    updateTimer(textView, sharedPref.getSleepTime());
                }
            }, 1000);
        }
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (fragmentManager.getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            if (Config.ENABLE_RTL_MODE) {
                if (viewPagerRTL.getCurrentItem() > 0) {
                    viewPagerRTL.setCurrentItem(0);
                } else {
                    exitDialog();
                }
            } else {
                if (viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(0);
                } else {
                    exitDialog();
                }
            }
        }
    }

    public void exitDialog() {

        if (adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.custom_exit_dialog, null);

            adsManager.loadNativeAdView(view, Config.NATIVE_AD_EXIT_DIALOG);

            final AlertDialog.Builder dialog;
            if (themePref.getCurrentTheme() == THEME_DARK) {
                dialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            } else {
                dialog = new AlertDialog.Builder(this);
            }
            dialog.setView(view);
            dialog.setCancelable(false);
            dialog.setPositiveButton(getResources().getString(R.string.dialog_option_quit), (dialogInterface, i) -> {
                finish();
                adsManager.destroyBannerAd();
                if (isServiceRunning()) {
                    Intent stop = new Intent(this, RadioPlayerService.class);
                    stop.setAction(RadioPlayerService.ACTION_STOP);
                    startService(stop);
                    Log.d("RADIO_SERVICE", "Service Running");
                } else {
                    Log.d("RADIO_SERVICE", "Service Not Running");
                }
            });

            dialog.setNegativeButton(getResources().getString(R.string.dialog_option_minimize), (dialogInterface, i) -> minimizeApp());

            dialog.setNeutralButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> {

            });
            dialog.show();

        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setIcon(R.mipmap.ic_launcher);
            dialog.setTitle(R.string.app_name);
            dialog.setMessage(getResources().getString(R.string.message));
            dialog.setPositiveButton(getResources().getString(R.string.dialog_option_quit), (dialogInterface, i) -> {
                finish();
                adsManager.destroyBannerAd();
                if (isServiceRunning()) {
                    Intent stop = new Intent(MainActivity.this, RadioPlayerService.class);
                    stop.setAction(RadioPlayerService.ACTION_STOP);
                    startService(stop);
                    Log.d("RADIO_SERVICE", "Service Running");
                } else {
                    Log.d("RADIO_SERVICE", "Service Not Running");
                }
            });

            dialog.setNegativeButton(getResources().getString(R.string.dialog_option_minimize), (dialogInterface, i) -> minimizeApp());

            dialog.setNeutralButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> {

            });
            dialog.show();
        }

    }

    public void minimizeApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void openCategoryDetail(String category_id, String category_name, String category_image, String radio_count) {
        FragmentCategoryDetail fragment = new FragmentCategoryDetail();
        Bundle args = new Bundle();
        args.putString("category_id", category_id);
        args.putString("category_name", category_name);
        args.putString("category_image", category_image);
        args.putString("radio_count", radio_count);
        fragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.fragment_container, fragment).addToBackStack("category_detail");
        transaction.commit();
    }

    public void openFragmentSearch() {
        FragmentSearch fragment = new FragmentSearch();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.fragment_container, fragment).addToBackStack("search");
        transaction.commit();
    }

    public void openFragmentSettings() {
        FragmentSettings fragment = new FragmentSettings();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, fragment).addToBackStack("settings");
        transaction.commit();
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RadioPlayerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void hideKeyboard() {
        try {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if (((getCurrentFocus() != null) && ((getCurrentFocus().getWindowToken() != null)))) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void aboutDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
        View view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_about, null);

        ((TextView) view.findViewById(R.id.txt_app_version)).setText(getString(R.string.sub_about_app_version) + " " + BuildConfig.VERSION_NAME);

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.option_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    public void themeColor() {
        if (themePref.getCurrentTheme() == THEME_LIGHT) {
            toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorLight));
            toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBackgroundDark));
            lytCollapse.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorLight));
            imgPrevious.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black), PorterDuff.Mode.SRC_IN);
            imgNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black), PorterDuff.Mode.SRC_IN);
            imgPlay.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black), PorterDuff.Mode.SRC_IN);
            bgLine.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            Tools.lightStatusBar(MainActivity.this, true);
        } else if (themePref.getCurrentTheme() == THEME_PRIMARY) {
            toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            lytCollapse.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorLight));
            imgPrevious.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black), PorterDuff.Mode.SRC_IN);
            imgNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black), PorterDuff.Mode.SRC_IN);
            imgPlay.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.black), PorterDuff.Mode.SRC_IN);
            bgLine.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            Tools.primaryNavigation(this);
        } else if (themePref.getCurrentTheme() == THEME_DARK) {
            toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarDark));
            lytCollapse.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorToolbarDark));
            imgPrevious.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            imgNext.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            imgPlay.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            bgLine.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            Tools.darkNavigation(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        initComponent();
        adsManager.resumeBannerAd(Config.BANNER_HOME);
        themeColor();
    }

    @Override
    protected void onDestroy() {
        Constant.is_app_open = false;
        adsManager.destroyBannerAd();
        super.onDestroy();
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    public void loadAdMobInterstitialAd() {
        if (Config.INTERSTITIAL_RADIO_LIST == 1) {
            InterstitialAd.load(this, adsPref.getAdMobInterstitialId(), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    adMobInterstitialAd = interstitialAd;
                    adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            loadAdMobInterstitialAd();
                            if (isShowingInterstitialAd) {
                                isShowingInterstitialAd = false;
                                startRadioServices(sharedPref.getCurrentRadioPosition());
                            }
                            Log.d(TAG, "resume playing radio");
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            Log.d(TAG, "The ad failed to show.");
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            adMobInterstitialAd = null;
                            Log.d(TAG, "The ad was shown.");
                        }
                    });
                    Log.i(TAG, "onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.i(TAG, loadAdError.getMessage());
                    adMobInterstitialAd = null;
                    Log.d(TAG, "Failed load AdMob Interstitial Ad");
                }
            });
        }
    }

    public void showAdMobInterstitialAd(int position) {
        if (Config.INTERSTITIAL_RADIO_LIST == 1) {
            if (adMobInterstitialAd != null) {
                if (counter == adsPref.getInterstitialAdInterval()) {
                    adMobInterstitialAd.show(this);
                    counter = 1;
                    isShowingInterstitialAd = true;
                    stopRadioServices(position);
                    Log.d(TAG, "stop radio services while displaying interstitial ad");
                } else {
                    counter++;
                    startRadioServices(position);
                    Log.d(TAG, "start radio services");
                }
            } else {
                startRadioServices(position);
                Log.d(TAG, "null");
            }
        } else {
            startRadioServices(position);
        }
    }

    public void startRadioServices(int position) {
        Intent intent = new Intent(MainActivity.this, RadioPlayerService.class);
        RadioPlayerService.createInstance().initializeRadio(MainActivity.this, Constant.item_radio.get(position));
        intent.setAction(RadioPlayerService.ACTION_PLAY);
        startService(intent);
    }

    public void stopRadioServices(int position) {
        Intent intent = new Intent(MainActivity.this, RadioPlayerService.class);
        RadioPlayerService.createInstance().initializeRadio(MainActivity.this, Constant.item_radio.get(position));
        intent.setAction(RadioPlayerService.ACTION_STOP);
        startService(intent);
    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
            Log.d(TAG, "in app update token");
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> Log.d(TAG, "Success")
                    ).addOnFailureListener(failure -> Log.d(TAG, "Rating Failed"));
                }
            }).addOnFailureListener(failure -> Log.d(TAG, "In-App Request Failed " + failure));
            Log.d(TAG, "in app token complete, show in app review if available");
        }
        Log.d(TAG, "in app review token : " + sharedPref.getInAppReviewToken());
    }

    private void inAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, Constant.IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_cancel_update));
            } else if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.msg_success_update));
            } else {
                showSnackBar(getString(R.string.msg_failed_update));
                inAppUpdate();
            }
        }
    }

    public void selectRadio() {
        if (Config.ENABLE_RTL_MODE) {
            viewPagerRTL.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(1);
        }
    }

    public void selectCategory() {
        if (Config.ENABLE_RTL_MODE) {
            viewPagerRTL.setCurrentItem(2);
        } else {
            viewPager.setCurrentItem(2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (themePref.getCurrentTheme() == THEME_LIGHT) {
            getMenuInflater().inflate(R.menu.menu_main_light, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.search) {
            openFragmentSearch();
            return true;
        } else if (menuItem.getItemId() == R.id.settings) {
            openFragmentSettings();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void showBottomSheet(Radio radio) {
        tools.showBottomSheetDialog(this, parentView, radio);
    }

    public void showSnackBar(String msg) {
        Snackbar.make(parentView, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void onItemRadioClick(final ArrayList<Radio> radios, int position) {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(ADMOB)) {
            Constant.item_radio.clear();
            Constant.item_radio.addAll(radios);
            Constant.position = position;
            sharedPref.setCurrentRadioPosition(position);
            showAdMobInterstitialAd(position);
        } else {
            Constant.item_radio.clear();
            Constant.item_radio.addAll(radios);
            Constant.position = position;
            Intent intent = new Intent(MainActivity.this, RadioPlayerService.class);
            RadioPlayerService.createInstance().initializeRadio(MainActivity.this, Constant.item_radio.get(position));
            intent.setAction(RadioPlayerService.ACTION_PLAY);
            startService(intent);
            showInterstitialAd();
        }
        hideSeekBar();
    }

}
