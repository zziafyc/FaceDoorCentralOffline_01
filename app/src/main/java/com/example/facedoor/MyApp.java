package com.example.facedoor;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.example.facedoor.db.DBManage;
import com.example.facedoor.exception.CrashHandler;
import com.iflytek.cloud.SpeechUtility;

import java.util.ArrayList;
import java.util.List;

public class MyApp extends Application {

    public static final String adminId = "admin1";
    public static final String DBIP_KEY = "dbip";
    public static final String DOORIP_KEY = "doorip";
    public static final String DOOR_CONTROLLER = "doorcontroller";
    public static final String OPEN_TIME = "opentime";
    public static final String PLATFORM_IP = "platformip";
    public static final String DOOR_NUM = "doornum";
    public static final String DB_AGENT = "dbagent";
    public static final String VOICE_VALUE = "voiceValue";
    public static final String DETECT_TIME_VALUE = "detectTimeValue";
    public static final String FACEONLY = "faceonly";
    public static final String FACEDETECT = "faceDetect";
    public static final String CONFIG = "config";
    public static final String PERSONNAL = "personnal";
    public static final String APPID = "pc20onli";

    public static DBManage db;
    private List<Activity> activities = new ArrayList<Activity>();
    Intent serviceIntent;
    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SpeechUtility.createUtility(this, "appid=" + getString(R.string.appid));
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(getApplicationContext());

        //启动dds服务
        serviceIntent = new Intent(this, DDSService.class);
        serviceIntent.setAction("start");
        startService(serviceIntent);
    }

    public static DBManage getDBManage(Context context) {
        if (db == null) {
            db = new DBManage(context);
        }
        return db;
    }


    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        for (Activity activity : activities) {
            activity.finish();
        }
        db.closeDB();
        stopService(serviceIntent);
        System.exit(1);
    }
}
