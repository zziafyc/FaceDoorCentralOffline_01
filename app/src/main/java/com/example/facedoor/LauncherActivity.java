package com.example.facedoor;

import android.content.Intent;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.example.facedoor.base.BaseAppCompatActivity;

public class LauncherActivity extends BaseAppCompatActivity {
    private static final String TAG = "LauncherActivity";

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_launcher;
    }

    @Override
    protected void initViewsAndEvents() {
        new Thread() {
            public void run() {
                checkDDSReady();
            }
        }.start();
    }


    public void checkDDSReady() {
        while (true) {
            if (DDS.getInstance().isInitComplete()) {
                Intent intent = new Intent(getApplicationContext(), FaceIndexActivity.class);
                startActivity(intent);
                finish();
                break;
            } else {
                AILog.w(TAG, "waiting  init complete finish...");
            }
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}