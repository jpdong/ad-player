package com.eeseetech.nagrand.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.MainActivity;
import com.eeseetech.nagrand.service.MultiDisplayService;

public class BootedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //context.startService(new Intent(context, MultiDisplayService.class));
        Log.d(Global.TAG, "BootedReceiver/onReceive:");
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }
}
