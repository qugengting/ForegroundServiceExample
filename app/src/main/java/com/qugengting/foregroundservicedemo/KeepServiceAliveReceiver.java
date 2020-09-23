package com.qugengting.foregroundservicedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.Date;

public class KeepServiceAliveReceiver extends BroadcastReceiver {

    private static final String TAG = KeepServiceAliveReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive ================================== " + intent.getAction());
        String error = "\n==========================\n" + intent.getAction() + "\n=========================\n" +
                DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm:ss")
                +"\n==========================\n=========================\n";
        FileUtil.writeFile(FileUtil.getFileName(context), error, true);
        Intent service = new Intent(context, ForegroundService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }
}
