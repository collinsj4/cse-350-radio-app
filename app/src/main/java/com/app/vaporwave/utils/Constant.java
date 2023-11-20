package com.app.vaporwave.utils;

import android.graphics.Bitmap;

import com.app.vaporwave.models.Radio;
import com.vhall.android.exoplayer2.SimpleExoPlayer;

import java.io.Serializable;
import java.util.ArrayList;

public class Constant implements Serializable {

    public static final int IMMEDIATE_APP_UPDATE_REQ_CODE = 124;
    private static final long serialVersionUID = 1L;
    public static final int PERMISSIONS_REQUEST = 102;
    public static final String LOCALHOST_ADDRESS = "http://192.168.1.2";
    public static final int DELAY_SPLASH = 2000;
    public static final int DELAY_PROGRESS = 100;
    public static final int DELAY_CLICK = 150;
    public static final int DELAY_ACTION_CLICK = 2000;
    public static final int WAITING_TIME_NEXT_ITEM_CLICK = 1000;

    public static String metadata;
    public static String albumArt;
    public static SimpleExoPlayer exoPlayer;
    public static Boolean is_playing = false;
    public static Boolean radio_type = true;
    public static Boolean is_app_open = false;
    public static ArrayList<Radio> item_radio = new ArrayList<>();
    public static int position = 0;
    public static Bitmap colorBitmap;
    public static Boolean isPlayerExpanded = false;

    public static Boolean progressVisibility = false;

    public static final int MAX_NUMBER_OF_NATIVE_AD_DISPLAYED = 50;

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_PRIMARY = 2;

}