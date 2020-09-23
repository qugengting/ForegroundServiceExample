package com.qugengting.foregroundservicedemo;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.common.library.net.NetWorkRetrofit;

import java.util.List;

/**
 * @author:xuruibin
 * @date:2020/8/25 Description:
 */
public class MainActivity extends CheckPermissionsActivity {

    private TextView mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = findViewById(R.id.text);

        NetWorkRetrofit.getInstance().init(getApplication());

        boolean b = isServiceExisted(this, "com.qugengting.foregroundservicedemo.ForegroundService");
        if (b) {
            mBtn.setText("定时任务已开启");
            findViewById(R.id.btn).setEnabled(false);
        }
    }

    private boolean isServiceExisted(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;

            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public void alarmTask(View view) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ForegroundService.class);

        intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        mBtn.setText("定时任务已开启");
        view.setEnabled(false);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, pendingIntent);
    }
}
