package com.app.vaporwave.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.services.RadioPlayerService;

public class SleepTimeReceiver extends BroadcastReceiver {

    SharedPref sharedPref;

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPref = new SharedPref(context);

        if (sharedPref.getIsSleepTimeOn()) {
            sharedPref.setSleepTime(false, 0,0);
        }

        Intent intent_close = new Intent(context, RadioPlayerService.class);
        intent_close.setAction(RadioPlayerService.ACTION_STOP);
        context.startService(intent_close);
    }
}