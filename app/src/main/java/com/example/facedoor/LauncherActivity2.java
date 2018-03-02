package com.example.facedoor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;


/**
 * Disclaim
 * <p>
 * This program is the property of AI Speech Ltd. It shall be communicated to
 * authorized personnel only. It is not to be disclosed outside the group without
 * prior written consent. If you are not sure if you’re authorized to read this
 * program, please contact info@aispeech.com before reading.
 * <p>
 * Created by jinrui.gan on 17-3-12.
 */

public class LauncherActivity2 extends Activity {

    private static final String TAG = "LauncherActivity";

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        new Thread() {
            public void run() {
                checkDDSReady();
            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ddsdemo.intent.action.auth_success");
        intentFilter.addAction("ddsdemo.intent.action.auth_failed");
        registerReceiver(authReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(authReceiver);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void checkDDSReady() {
        while (true) {
            if (DDS.getInstance().getInitStatus() != DDS.INIT_COMPLETE_NONE) {
                try {
                    if (DDS.getInstance().isAuthSuccess()) {
                        gotoMainActivity();
                        break;
                    } else {
                        showDoAuthDialog();
                    }
                } catch (DDSNotInitCompleteException e) {
                    e.printStackTrace();
                }
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

    private void gotoMainActivity() {
        Intent intent = new Intent(this, FaceIndexActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDoAuthDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity2.this);
                builder.setMessage("未授权");
                builder.setPositiveButton("做一次授权", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            DDS.getInstance().doAuth();
                        } catch (DDSNotInitCompleteException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });

                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog = builder.create();
                dialog.show();
            }
        });
    }

    private BroadcastReceiver authReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), "ddsdemo.intent.action.auth_success")) {
                gotoMainActivity();
            } else if (TextUtils.equals(intent.getAction(), "ddsdemo.intent.action.auth_failed")) {
                showDoAuthDialog();
            }
        }
    };
}
