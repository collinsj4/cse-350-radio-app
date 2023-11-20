package com.app.vaporwave.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.session.MediaButtonReceiver;

import com.app.vaporwave.BuildConfig;
import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.callbacks.CallbackAlbumArt;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.metadata.IcyHttpDataSourceFactory;
import com.app.vaporwave.models.AlbumArt;
import com.app.vaporwave.models.Radio;
import com.app.vaporwave.rests.RestAdapter;
import com.app.vaporwave.services.parser.URLParser;
import com.app.vaporwave.utils.Constant;
import com.app.vaporwave.utils.HttpsTrustManager;
import com.app.vaporwave.utils.Tools;
import com.vhall.android.exoplayer2.ExoPlaybackException;
import com.vhall.android.exoplayer2.ExoPlayerFactory;
import com.vhall.android.exoplayer2.PlaybackParameters;
import com.vhall.android.exoplayer2.Player;
import com.vhall.android.exoplayer2.Timeline;
import com.vhall.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.vhall.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.vhall.android.exoplayer2.source.ExtractorMediaSource;
import com.vhall.android.exoplayer2.source.MediaSource;
import com.vhall.android.exoplayer2.source.TrackGroupArray;
import com.vhall.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.vhall.android.exoplayer2.source.hls.HlsMediaSource;
import com.vhall.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.vhall.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.vhall.android.exoplayer2.trackselection.TrackSelectionArray;
import com.vhall.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.vhall.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class RadioPlayerService extends Service {

    public static final String TAG = "RadioPlayerService";
    static private final int NOTIFICATION_ID = 1;
    @SuppressLint("StaticFieldLeak")
    static private RadioPlayerService service;
    @SuppressLint("StaticFieldLeak")
    static private Context context;
    static NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    static SharedPref sharedPref;
    static Radio radio;
    private boolean clicked;
    private Boolean isCanceled = false;
    boolean isCounterRunning = false;
    RemoteViews bigViews, smallViews;
    Tools tools;
    Bitmap bitmap;
    ComponentName componentName;
    AudioManager mAudioManager;
    PowerManager.WakeLock mWakeLock;
    Call<CallbackAlbumArt> callbackCall = null;
    MediaSessionCompat mMediaSession;
    Radio obj;
    LoadSong loadSong;

    public static final String ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP";
    public static final String ACTION_PLAY = BuildConfig.APPLICATION_ID + ".action.PLAY";
    public static final String ACTION_PREVIOUS = BuildConfig.APPLICATION_ID + ".action.PREVIOUS";
    public static final String ACTION_NEXT = BuildConfig.APPLICATION_ID + ".action.NEXT";
    public static final String ACTION_TOGGLE = BuildConfig.APPLICATION_ID + ".action.TOGGLE_PLAYPAUSE";

    static public void initialize(Context context) {
        RadioPlayerService.context = context;
        RadioPlayerService.sharedPref = new SharedPref(context);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void initializeRadio(Context context, Radio station) {
        RadioPlayerService.context = context;
        RadioPlayerService.sharedPref = new SharedPref(context);
        radio = station;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static RadioPlayerService getInstance() {
        return service;
    }

    public static RadioPlayerService createInstance() {
        if (service == null) {
            service = new RadioPlayerService();
        }
        return service;
    }

    public Boolean isPlaying() {
        if (service == null) {
            return false;
        } else {
            if (Constant.exoPlayer != null) {
                return Constant.exoPlayer.getPlayWhenReady();
            } else {
                return false;
            }
        }
    }

    @Override
    public void onCreate() {
        tools = new Tools(context);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        componentName = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(componentName);

        LocalBroadcastManager.getInstance(this).registerReceiver(onCallIncome, new IntentFilter("android.intent.action.PHONE_STATE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(onHeadPhoneDetect, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        Constant.exoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        Constant.exoPlayer.addListener(eventListener);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null)
            try {
                switch (action) {
                    case ACTION_STOP:
                        if (isPlaying()) {
                            new Handler(Looper.getMainLooper()).postDelayed(() -> stop(intent), 2000);
                            Constant.exoPlayer.removeListener(eventListener);
                            pause();
                        } else {
                            stop(intent);
                        }
                        break;
                    case ACTION_PLAY:
                        newPlay();
                        break;
                    case ACTION_TOGGLE:
                        togglePlayPause();
                        break;
                    case ACTION_PREVIOUS:
                        if (Tools.isNetworkAvailable(context)) {
                            previous();
                        } else {
                            Toast.makeText(context, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case ACTION_NEXT:
                        if (Tools.isNetworkAvailable(context)) {
                            next();
                        } else {
                            Toast.makeText(context, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        if (isPlaying()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ((MainActivity) context).finish();
                stop(intent);
            }, 2000);
            pause();
        } else {
            ((MainActivity) context).finish();
            stop(intent);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadSong extends AsyncTask<String, Void, Boolean> {

        MediaSource mediaSource;

        protected void onPreExecute() {
            ((MainActivity) context).setBuffer(true);
            ((MainActivity) context).changeSongName(Constant.item_radio.get(Constant.position).category_name);
        }

        protected Boolean doInBackground(final String... args) {
            try {
                HttpsTrustManager.allowAllSSL();
                String url = Constant.item_radio.get(Constant.position).radio_url;
                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), null, icy);
                if (url.contains(".m3u8") || url.contains(".M3U8")) {
                    mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                            .setAllowChunklessPreparation(false)
                            .setExtractorFactory(new DefaultHlsExtractorFactory(DefaultTsPayloadReaderFactory.FLAG_IGNORE_H264_STREAM))
                            .createMediaSource(Uri.parse(url));
                } else if (url.contains(".m3u") || url.contains("yp.shoutcast.com/sbin/tunein-station.m3u?id=")) {
                    url = URLParser.getUrl(url);
                    mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                            .setExtractorsFactory(new DefaultExtractorsFactory())
                            .createMediaSource(Uri.parse(url));
                } else if (url.contains(".pls") || url.contains("listen.pls?sid=") || url.contains("yp.shoutcast.com/sbin/tunein-station.pls?id=")) {
                    url = URLParser.getUrl(url);
                    mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                            .setExtractorsFactory(new DefaultExtractorsFactory())
                            .createMediaSource(Uri.parse(url));
                } else {
                    mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                            .setExtractorsFactory(new DefaultExtractorsFactory())
                            .createMediaSource(Uri.parse(url));
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (context != null) {
                super.onPostExecute(aBoolean);
                Constant.exoPlayer.seekTo(Constant.exoPlayer.getCurrentWindowIndex(), Constant.exoPlayer.getCurrentPosition());
                Constant.exoPlayer.prepare(mediaSource, false, false);
                Constant.exoPlayer.setPlayWhenReady(true);
                if (!aBoolean) {
                    ((MainActivity) context).setBuffer(false);
                    Toast.makeText(context, getString(R.string.error_loading_radio), Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    Player.EventListener eventListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                next();
            }
            if (playbackState == Player.STATE_READY && playWhenReady) {
                if (!isCanceled) {
                    ((MainActivity) context).seekBarUpdate();
                    ((MainActivity) context).setBuffer(false);
                    if (mBuilder == null) {
                        createNotification();
                    } else {
                        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                        updateNotificationPlay(Constant.exoPlayer.getPlayWhenReady());
                    }

                    Constant.radio_type = !Constant.item_radio.get(Constant.position).radio_type.equals("mp3");
                    updateNotificationAlbumArt(sharedPref.getBaseUrl() + "/upload/" + Constant.item_radio.get(Constant.position).radio_image);
                    updateNotificationMetadata(Constant.item_radio.get(Constant.position).category_name);

                    changePlayPause(true);

                    if (Config.ENABLE_RADIO_TIMEOUT) {
                        if (isCounterRunning) {
                            mCountDownTimer.cancel();
                        }
                    }

                } else {
                    isCanceled = false;
                    stopExoPlayer();
                }
            }
            if (playWhenReady) {
                if (!mWakeLock.isHeld()) {
                    mWakeLock.acquire(60000);
                }
            } else {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            stop(true);
            if (Config.ENABLE_RADIO_TIMEOUT) {
                if (isCounterRunning) {
                    mCountDownTimer.cancel();
                }
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    };

    private void newPlay() {
        loadSong = new LoadSong();
        loadSong.execute();

        if (Config.ENABLE_RADIO_TIMEOUT) {
            if (isCounterRunning) {
                mCountDownTimer.cancel();
            }
            mCountDownTimer.start();
        }

    }

    CountDownTimer mCountDownTimer = new CountDownTimer(Config.RADIO_TIMEOUT_CONNECTION, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            isCounterRunning = true;
            Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            isCounterRunning = false;
            stop(true);
        }
    };

//    private void updateNotificationImageAlbumArt(String artWorkUrl) {
//        new Thread(() -> {
//            try {
//                getBitmapFromURL(artWorkUrl);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    mBuilder.setLargeIcon(bitmap);
//                } else {
//                    bigViews.setImageViewBitmap(R.id.img_notification, bitmap);
//                    smallViews.setImageViewBitmap(R.id.status_bar_album_art, bitmap);
//                }
//                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            new Handler(Looper.getMainLooper()).post(() -> mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build()));
//        }).start();
//    }

//    private void updateNotificationMetadata() {
//        obj = Constant.item_radio.get(Constant.position);
//        updateNotificationImageAlbumArt(sharedPref.getBaseUrl() + "/upload/" +obj.radio_image);
//        updateNotificationMetadata(obj.radio_name, obj.category_name);
//        ((MainActivity) context).changeSongName(obj.category_name);
//        Log.d(TAG, "setDefaultImageIfMetadataIsEmpty");
//    }

//    private void updateNotificationMetadata(String radio_name, String metadata) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            mMediaSession = new MediaSessionCompat(context, getString(R.string.app_name));
//            mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
//            mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
//                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, radio_name)
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata)
//                    .build());
//            mBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mMediaSession.getSessionToken())
//                    .setShowCancelButton(true)
//                    .setShowActionsInCompactView(0, 1)
//                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)));
//        }
//    }

    private String getUserAgent() {

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

    private void changePlayPause(Boolean play) {
        ((MainActivity) context).changePlayPause(play);
    }

    private void togglePlayPause() {

        if (clicked) {
            return;
        }
        clicked = true;
        new Handler().postDelayed(() -> clicked = false, 1000);

        if (Constant.exoPlayer.getPlayWhenReady()) {
            pause();
        } else {
            if (Tools.isNetworkAvailable(context)) {
                play();
            } else {
                Toast.makeText(context, getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pause() {
        Constant.exoPlayer.setPlayWhenReady(false);
        changePlayPause(false);
        updateNotificationPlay(false);
    }

    private void play() {
        Constant.exoPlayer.setPlayWhenReady(true);
        Constant.exoPlayer.seekTo(Constant.exoPlayer.getCurrentWindowIndex(), Constant.exoPlayer.getCurrentPosition());
        changePlayPause(true);
        updateNotificationPlay(true);
        ((MainActivity) context).seekBarUpdate();
    }

    private void stop(boolean showMessage) {
        if (Constant.exoPlayer != null) {
            try {
                mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(onCallIncome);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(onHeadPhoneDetect);
                mAudioManager.unregisterMediaButtonEventReceiver(componentName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            changePlayPause(false);
            stopExoPlayer();
            service = null;
            stopForeground(true);
            stopSelf();
            ((MainActivity) context).setBuffer(false);
            ((MainActivity) context).changePlayPause(false);

            if (showMessage) {
                Toast.makeText(context, getString(R.string.error_loading_radio), Toast.LENGTH_SHORT).show();
            }
        }
    }

//    private void newPlay() {
//        startSong();
//    }

    private void next() {
        Tools.getPosition(true);
        radio = Constant.item_radio.get(Constant.position);
        newPlay();
    }

    private void previous() {
        Tools.getPosition(false);
        radio = Constant.item_radio.get(Constant.position);
        newPlay();
    }

    public void stop(Intent intent) {
        if (Constant.exoPlayer != null) {
            try {
                mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(onCallIncome);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(onHeadPhoneDetect);
                mAudioManager.unregisterMediaButtonEventReceiver(componentName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            changePlayPause(false);
            stopExoPlayer();
            service = null;
            stopService(intent);
            stopForeground(true);
            stopSelf();
            ((MainActivity) context).setBuffer(false);
            ((MainActivity) context).changePlayPause(false);
        }
    }

    public void stopExoPlayer() {
        if (Constant.exoPlayer != null) {
            Constant.exoPlayer.stop();
            //Constant.exoPlayer.addListener(listener);
        }
    }

    private void createNotification() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        int FLAG_PENDING_INTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            FLAG_PENDING_INTENT = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            FLAG_PENDING_INTENT = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        int FLAG_ACTION_INTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            FLAG_ACTION_INTENT = PendingIntent.FLAG_IMMUTABLE;
        } else {
            FLAG_ACTION_INTENT = 0;
        }

        int FLAG_STOP_INTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            FLAG_STOP_INTENT = PendingIntent.FLAG_IMMUTABLE;
        } else {
            FLAG_STOP_INTENT = PendingIntent.FLAG_CANCEL_CURRENT;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, FLAG_PENDING_INTENT);

        Intent previousIntent = new Intent(getApplicationContext(), RadioPlayerService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent pendingIntentPrevious = PendingIntent.getService(this, 0, previousIntent, FLAG_ACTION_INTENT);

        Intent playIntent = new Intent(this, RadioPlayerService.class);
        playIntent.setAction(ACTION_TOGGLE);
        PendingIntent pendingIntentPlay = PendingIntent.getService(this, 0, playIntent, FLAG_ACTION_INTENT);

        Intent nextIntent = new Intent(getApplicationContext(), RadioPlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getService(this, 0, nextIntent, FLAG_ACTION_INTENT);

        Intent closeIntent = new Intent(this, RadioPlayerService.class);
        closeIntent.setAction(ACTION_STOP);
        PendingIntent pendingIntentClose = PendingIntent.getService(this, 0, closeIntent, FLAG_STOP_INTENT);


        String NOTIFICATION_CHANNEL_ID = "your_single_app_channel_001";
        mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setTicker(radio.radio_name)
                .setContentTitle(radio.radio_name)
                .setContentText(Constant.metadata)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_radio_notif)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setOnlyAlertOnce(true);

        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);

            mMediaSession = new MediaSessionCompat(context, getString(R.string.app_name));
            mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

            mBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mMediaSession.getSessionToken())
                    .setShowCancelButton(true)
                            .setShowActionsInCompactView(0, 1, 2)
                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)))
                    .addAction(new NotificationCompat.Action(R.drawable.ic_noti_previous, "Previous", pendingIntentPrevious))
                    .addAction(new NotificationCompat.Action(R.drawable.ic_noti_pause, "Pause", pendingIntentPlay))
                    .addAction(new NotificationCompat.Action(R.drawable.ic_noti_next, "Next", pendingIntentNext))
                    .addAction(new NotificationCompat.Action(R.drawable.ic_noti_close, "Close", pendingIntentClose));
        } else {
            bigViews = new RemoteViews(getPackageName(), R.layout.lyt_notification_large);
            smallViews = new RemoteViews(getPackageName(), R.layout.lyt_notification_small);
            bigViews.setOnClickPendingIntent(R.id.img_notification_play, pendingIntentPlay);
            smallViews.setOnClickPendingIntent(R.id.status_bar_play, pendingIntentPlay);

            bigViews.setOnClickPendingIntent(R.id.img_notification_next, pendingIntentNext);
            smallViews.setOnClickPendingIntent(R.id.status_bar_next, pendingIntentNext);

            bigViews.setOnClickPendingIntent(R.id.img_notification_previous, pendingIntentPrevious);
            smallViews.setOnClickPendingIntent(R.id.status_bar_prev, pendingIntentPrevious);

            bigViews.setOnClickPendingIntent(R.id.img_notification_close, pendingIntentClose);
            smallViews.setOnClickPendingIntent(R.id.status_bar_collapse, pendingIntentClose);

            bigViews.setImageViewResource(R.id.img_notification_play, android.R.drawable.ic_media_pause);
            smallViews.setImageViewResource(R.id.status_bar_play, android.R.drawable.ic_media_pause);

            bigViews.setTextViewText(R.id.txt_notification_name, Constant.item_radio.get(Constant.position).radio_name);
            bigViews.setTextViewText(R.id.txt_notification_category, Constant.metadata);
            smallViews.setTextViewText(R.id.status_bar_track_name, Constant.item_radio.get(Constant.position).radio_name);
            smallViews.setTextViewText(R.id.status_bar_artist_name, Constant.metadata);

            bigViews.setImageViewResource(R.id.img_notification, R.mipmap.ic_launcher);
            smallViews.setImageViewResource(R.id.status_bar_album_art, R.mipmap.ic_launcher);

            mBuilder.setCustomContentView(smallViews).setCustomBigContentView(bigViews);
        }

        startForeground(NOTIFICATION_ID, mBuilder.build());
    }

//    private void updateNotification() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mBuilder.setContentTitle(Constant.item_radio.get(Constant.position).radio_name);
//            mBuilder.setContentText(Constant.metadata);
//        } else {
//            bigViews.setTextViewText(R.id.txt_notification_name, Constant.item_radio.get(Constant.position).radio_name);
//            bigViews.setTextViewText(R.id.txt_notification_category, Constant.metadata);
//            smallViews.setTextViewText(R.id.status_bar_track_name, Constant.item_radio.get(Constant.position).radio_name);
//            smallViews.setTextViewText(R.id.status_bar_artist_name, Constant.metadata);
//        }
//        updateNotificationPlay(Constant.exoPlayer.getPlayWhenReady());
//    }

    @SuppressLint("RestrictedApi")
    private void updateNotificationPlay(Boolean isPlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            mBuilder.mActions.remove(1);
            Intent playIntent = new Intent(this, RadioPlayerService.class);
            playIntent.setAction(ACTION_TOGGLE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);
            if (isPlay) {
                mBuilder.mActions.add(1, new NotificationCompat.Action(R.drawable.ic_pause_white, "Pause", pendingIntent));
            } else {
                mBuilder.mActions.add(1, new NotificationCompat.Action(R.drawable.ic_play_arrow_white, "Play", pendingIntent));
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mBuilder.mActions.remove(1);
            Intent playIntent = new Intent(this, RadioPlayerService.class);
            playIntent.setAction(ACTION_TOGGLE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);
            if (isPlay) {
                mBuilder.mActions.add(1, new NotificationCompat.Action(R.drawable.ic_pause_white, "Pause", pendingIntent));
            } else {
                mBuilder.mActions.add(1, new NotificationCompat.Action(R.drawable.ic_play_arrow_white, "Play", pendingIntent));
            }
        } else {
            if (isPlay) {
                bigViews.setImageViewResource(R.id.img_notification_play, android.R.drawable.ic_media_pause);
                smallViews.setImageViewResource(R.id.status_bar_play, android.R.drawable.ic_media_pause);
            } else {
                bigViews.setImageViewResource(R.id.img_notification_play, android.R.drawable.ic_media_play);
                smallViews.setImageViewResource(R.id.status_bar_play, android.R.drawable.ic_media_play);
            }
        }
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public Radio getPlayingRadioStation() {
        return radio;
    }

    private void getBitmapFromURL(String src) {
        try {
            URL url = new URL(src.replace(" ", "%20"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
            Log.d("getBitmap", "load bitmap url : " + src);
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
            Log.d("getBitmap", "error : " + src);
        }
    }

    BroadcastReceiver onCallIncome = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (isPlaying()) {
                if (state != null) {
                    if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) || state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        Intent intent_stop = new Intent(context, RadioPlayerService.class);
                        intent_stop.setAction(ACTION_TOGGLE);
                        startService(intent_stop);
                        Toast.makeText(context, "there is an call!!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "whoops!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    BroadcastReceiver onHeadPhoneDetect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.is_playing) {
                togglePlayPause();
            }
        }
    };

    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Resume your media player here
                if (Config.RESUME_RADIO_ON_PHONE_CALL) {
                    togglePlayPause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying()) {
                    togglePlayPause();
                }
                break;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            Constant.exoPlayer.stop();
            Constant.exoPlayer.release();
            Constant.exoPlayer.removeListener(eventListener);
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            try {
                mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(onCallIncome);
                LocalBroadcastManager.getInstance(this).unregisterReceiver(onHeadPhoneDetect);
                mAudioManager.unregisterMediaButtonEventReceiver(componentName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public IcyHttpDataSourceFactory icy = new IcyHttpDataSourceFactory
            .Builder(getUserAgent())
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMillis(1000)
            .setIcyHeadersListener(icyHeaders -> {
            })
            .setIcyMetadataChangeListener(icyMetadata -> {
                try {
                    if (Config.DISPLAY_SONG_METADATA) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if ("".equalsIgnoreCase(icyMetadata.getStreamTitle())) {
                                updateNotificationMetadata(Constant.item_radio.get(Constant.position).category_name);
                                Log.d(TAG, "no metadata");
                            } else {
                                if (icyMetadata.getStreamTitle() != null) {
                                    updateNotificationMetadata(icyMetadata.getStreamTitle());
                                    requestAlbumArt(icyMetadata.getStreamTitle());
                                    Log.d(TAG, "metadata available : " + icyMetadata.getStreamTitle());
                                } else {
                                    updateNotificationMetadata(Constant.item_radio.get(Constant.position).category_name);
                                    Log.d(TAG, "metadata available null");
                                }
                            }
                        }, 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "metadata error : " + e.getMessage());
                }
            }).build();

    private void requestAlbumArt(String title) {
        if (Config.DISPLAY_ALBUM_ART_METADATA) {
            callbackCall = RestAdapter.createAlbumArtAPI().getAlbumArt(title, "music", 1);
            callbackCall.enqueue(new Callback<CallbackAlbumArt>() {
                public void onResponse(@NonNull Call<CallbackAlbumArt> call, @NonNull Response<CallbackAlbumArt> response) {
                    CallbackAlbumArt resp = response.body();
                    if (resp != null && resp.resultCount != 0) {
                        ArrayList<AlbumArt> albumArts = resp.results;
                        String artWorkUrl = albumArts.get(0).artworkUrl100.replace("100x100bb", "300x300bb");
                        ((MainActivity) context).changeAlbumArt(artWorkUrl);
                        updateNotificationAlbumArt(artWorkUrl);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> ((MainActivity) context).showImageAlbumArt(true), 100);
                        Log.d(TAG, "request album art success");
                    } else {
                        ((MainActivity) context).changeAlbumArt("");
                        updateNotificationAlbumArt("");
                        new Handler(Looper.getMainLooper()).postDelayed(() -> ((MainActivity) context).showImageAlbumArt(false), 100);
                        Log.d(TAG, "request album art failed");
                    }
                }

                public void onFailure(@NonNull Call<CallbackAlbumArt> call, @NonNull Throwable th) {
                    Log.d(TAG, "onFailure");
                }
            });
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void updateNotificationAlbumArt(String artWorkUrl) {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    getBitmapFromURL(artWorkUrl);
                    if (mBuilder != null) {
                        mBuilder.setLargeIcon(bitmap);
                        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }.execute();
    }

    private void updateNotificationMetadata(String title) {
        if (mBuilder != null) {
            ((MainActivity) context).changeSongName(title);
            mBuilder.setContentTitle(Constant.item_radio.get(Constant.position).radio_name);
            mBuilder.setContentText(title);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

}