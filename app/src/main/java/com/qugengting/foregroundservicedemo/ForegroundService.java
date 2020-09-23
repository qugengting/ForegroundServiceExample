package com.qugengting.foregroundservicedemo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.common.library.net.NetWorkRetrofit;
import com.common.library.net.bean.Qrcode;
import com.google.gson.Gson;

import java.util.Date;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author:xuruibin
 * @date:2020/8/25 Description:
 */
public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    private static final String NOTIFICATION_CHANNEL_ID = "999";
    private static final String NOTIFICATION_CHANNEL_NAME = "Test";
    private static final String NOTIFICATION_CHANNEL_DESC = "Testing";

    PowerManager.WakeLock wakeLock;
    WifiManager.WifiLock wifiLock;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String error1 = "==========================\n启动服务\n=========================\n" +
                DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm:ss")
                + "\n==========================\n=========================\n";
        FileUtil.writeFile(FileUtil.getFileName(getApplicationContext()), error1, true);

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Constants.ACTION.STARTFOREGROUND_ACTION:
                    Log.i(TAG, "Received Start Foreground Intent ");
                    startInForeground();
                    break;
                case Constants.ACTION.STOPFOREGROUND_ACTION:
                    Log.i(TAG, "Received Stop Foreground Intent");
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        } else {
            startInForeground();
            String error = "\n==========================\n==========================\n" +
                    "异常退出，重启服务\n=========================\n" +
                    DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm:ss")
                    + "\n==========================\n=========================\n";
            FileUtil.writeFile(FileUtil.getFileName(getApplicationContext()), error, true);
        }

        PowerManager pm = (PowerManager) getSystemService(Service.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "foregroundservice: mywakelock");

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Service.WIFI_SERVICE);       //保持wifi有效
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Gaode location");
        try {
            wakeLock.acquire(1000 * 60 * 60 * 24 * 365L);
            wifiLock.acquire();
        } catch (Exception e) {
            String error = "\n==========================\n==========================\nWakeLock错误信息" +
                    e.getMessage() + "\n=========================\n" +
                    DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm:ss")
                    + "\n==========================\n=========================\n";
            FileUtil.writeFile(FileUtil.getFileName(getApplicationContext()), error, true);
        }

        initLocation();
        startLocation();
        startAlarmTask();
        return START_STICKY;
    }

    private void startAlarmTask() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ForegroundService.class);
        intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, pendingIntent);
    }

    private static final long INTERVAL = 60000 * 10;

    private void startInForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("TEST正在运行")
                .setContentText("为您持续定位中")
                .setTicker("TICKER")
                .setWhen(System.currentTimeMillis()) // 设置该通知发生的时间
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "In onDestroy");
        destroyLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }

    public double longitude;//经度
    public double latitude;//纬度
    public String address = "";

    private static final long INTERVAL_TIME = 60000L;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    //***************高德定位**************************************************************************************

    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //初始化client
        if (locationClient == null) {
            locationClient = new AMapLocationClient(this.getApplicationContext());
        }
        if (locationOption == null) {
            locationOption = getDefaultOption();
        }
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(INTERVAL_TIME);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                address = location.getAddress();
                Log.e(TAG, "longitude: " + longitude + ", latitude: " + latitude + ", address: " + address);

                FileUtil.writeFile(FileUtil.getFileName(getApplicationContext()),
                        DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm:ss")
                                + "                    " + "\n"
                                + "longitude: " + longitude + "\n"
                                + ", latitude: " + latitude + "\n"
                                + ", address: " + address + "\n" + "=======================" + "\n",
                        true);
            } else {
                Log.e(TAG, "定位失败");
            }
        }
    };


    // 根据控件的选择，重新设置定位参数
    private void resetOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        /*
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(false);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(true);
        // 设置是否单次定位
        locationOption.setOnceLocation(true);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
        locationOption.setOnceLocationLatest(false);
        //设置是否使用传感器
        locationOption.setSensorEnable(false);
        //设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        try {
            // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
            locationOption.setInterval(INTERVAL_TIME);
            // 设置网络请求超时时间
            locationOption.setHttpTimeOut(30000);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
        resetOption();
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();
        internetTest();
    }

    /**
     * 网络测试
     */
    private void internetTest() {
        String content = "zhongliudong";
        String type = "0";
        String size = "500";
        String app_id = "r7nbnvfakxikpfrk";
        String app_secret = "NUs3UXFBNkpiaDlLTkUyUjJJQmlCQT09";
        NetWorkRetrofit.getInstance().getServiceAPI().qrcode(app_id, app_secret,
                content, size, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Qrcode>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Qrcode s) {
                        Gson gson = new Gson();
                        String result = gson.toJson(s);
                        Log.e(TAG, result);
                        FileUtil.writeFile(FileUtil.getFileName(getApplicationContext()),
                                DateUtils.date2Str(new Date(), "yyyy-MM-dd HH:mm:ss")
                                        + "                    " + "\n"
                                        + "content: " + s.getData().getContent() + "\n"
                                        + "=======================" + "\n",
                                true);
                    }
                });
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != locationClient) {
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }
}
