package com.example.facedoor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.aispeech.ailog.AILog;
import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.DDSConfig;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dds.update.DDSUpdateListener;
import com.aispeech.dui.dds.utils.PrefUtil;
import com.example.facedoor.base.BaseAppCompatActivity;
import com.example.facedoor.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;

/**
 * Created by fyc on 2017/11/2.
 */

public class FaceIndexActivity extends BaseAppCompatActivity implements DialogInterface.OnClickListener {
    @Bind(R.id.afi_click_img)
    ImageView clickImg;
    @Bind(R.id.img_more)
    ImageView moreImg;
    @Bind(R.id.wakeTv)
    TextView wakeTv;

    MyReceiver receiver;
    private MyMessageObserver mMessageObserver;
    private EditText editText;
    private static final String PASS_WORD = "123";
    private PopupWindow mPopupWindow;
    private static final String TAG = "fyc";
    private String wakeUpString = "";
    private Dialog dialog;
    private boolean isActivityShowing = false;
    private Handler mHandler = new Handler();

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_face_index;
    }

    @Override
    public void initViewsAndEvents() {
        initEvents();
        initListener();
    }

    private void initListener() {
        clickImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FaceIndexActivity.this, VideoDetect.class);
                startActivity(intent);
            }
        });
        moreImg.setVisibility(View.VISIBLE);
        moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPop(FaceIndexActivity.this, moreImg);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            editText = new EditText(FaceIndexActivity.this);
            new AlertDialog.Builder(FaceIndexActivity.this).setTitle("请输入密码").setIcon(R.drawable.ic_launcher)
                    .setView(editText).setPositiveButton("确定", FaceIndexActivity.this).setNegativeButton("取消", null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (editText.getText().toString().equals(PASS_WORD)) {
            Intent intent = new Intent(FaceIndexActivity.this, MainActivity.class);
            startActivity(intent);
            mPopupWindow.dismiss();
            finish();
        } else {
            Toast.makeText(FaceIndexActivity.this, "密码错", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPop(final Activity context, View parent) {
        final View contentView = LayoutInflater.from(context).inflate(R.layout.pop_exit, null, false);
        TextView textView = (TextView) contentView.findViewById(R.id.exit);
        textView.setText("进入管理员页");
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText = new EditText(FaceIndexActivity.this);
                new AlertDialog.Builder(FaceIndexActivity.this).setTitle("请输入密码").setIcon(R.drawable.ic_launcher)
                        .setView(editText).setPositiveButton("确定", FaceIndexActivity.this).setNegativeButton("取消", null).show();
            }
        });
        mPopupWindow = new PopupWindow(contentView, 150, 40, true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.showAsDropDown(parent, 20, 10);

    }

    private void initEvents() {
        //广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("ddsdemo.intent.action.init_complete");
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);

        //消息记录监听
        mMessageObserver = new MyMessageObserver();
    }

    //定义广播
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getAction();
            if (name.equals("ddsdemo.intent.action.init_complete")) {
                Log.e("fyc", "收到广播，成功初始化");
                sendHiMessage();
                enableWakeIfNecessary();
            }
        }
    }

    //定义消息观察者
    class MyMessageObserver implements MessageObserver {
        @Override
        public void onMessage(String message, String data) {
            Log.e(TAG, "message : " + message + " data : " + data);

            if ("context.output.text".equals(message)) {
                String text = "";
                try {
                    JSONObject jo = new JSONObject(data);
                    text = jo.optString("text", "");
                    Log.e(TAG, "mOutputText : " + text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if ("context.input.text".equals(message)) {
                String text = "";
                try {
                    JSONObject jo = new JSONObject(data);
                    if (jo.has("text")) {
                        text = jo.optString("text", "");
                    }
                    Log.e(TAG, "mInputText : " + text);
                    if (text.equals(wakeUpString)) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(FaceIndexActivity.this, VideoDetect.class);
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if ("context.widget.content".equals(message)) {

            }
        }
    }

    public void sendHiMessage() {
        String[] wakeupWords = new String[0];
        try {
            wakeupWords = DDS.getInstance().getAgent().getWakeupWords();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
        String hiStr = "";
        if (wakeupWords != null && wakeupWords.length == 2) {
            hiStr = getString(R.string.hi_str2, wakeupWords[0], wakeupWords[1]);
            wakeUpString = wakeupWords[0];
        } else if (wakeupWords != null && wakeupWords.length > 0) {
            hiStr = getString(R.string.hi_str, wakeupWords[0]);
            wakeUpString = wakeupWords[0];
        } else {
            wakeUpString = "你好小驰";
        }
        if (!StringUtils.isEmpty(wakeUpString)) {
            wakeTv.setText("点击或者读出“" + wakeUpString + "“");
        }
        Log.e(TAG, "sendHiMessage: " + hiStr);
    }

    public void enableWakeIfNecessary() {
        try {
            DDS.getInstance().getAgent().enableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    public void disableWakeIfNecessary() {
        try {
            DDS.getInstance().getAgent().stopDialog();
            DDS.getInstance().getAgent().disableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    private DDSUpdateListener ddsUpdateListener = new DDSUpdateListener() {
        @Override
        public void onUpdateFound(String detail) {
            final String str = detail;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showNewVersionDialog(str);
                }
            });

            try {
                DDS.getInstance().getAgent().speak("发现新版本,正在为您更新", 1);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpdateFinish() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showUpdateFinishedDialog();
                }
            });

            try {
                DDS.getInstance().getAgent().speak("更新成功", 1);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloadProgress(float progress) {
            Log.d(TAG, "onDownloadProgress :" + progress);
        }

        @Override
        public void onError(int what, String error) {
            String productId = PrefUtil.getString(getApplicationContext(), DDSConfig.K_PRODUCT_ID);
            if (what == 70319) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProductNeedUpdateDialog();
                    }
                });

            } else {
                Log.e(TAG, "UPDATE ERROR : " + error);
            }
        }

        @Override
        public void onUpgrade(String version) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showApkUpdateDialog();
                }
            });

        }
    };

    private MessageObserver resourceUpdatedMessageObserver = new MessageObserver() {
        @Override
        public void onMessage(String message, String data) {
            try {
                DDS.getInstance().getUpdater().update(ddsUpdateListener);
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }
    };

    protected void showProductNeedUpdateDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new android.support.v7.app.AlertDialog.Builder(this).setCancelable(false).setTitle(R.string
                .update_product_title)
                .setMessage(R.string.update_product_message).setPositiveButton(R.string.update_product_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.update_product_cancel, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }

    protected void showNewVersionDialog(final String info) {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setTitle(getString(R.string.dds_update_found_title));
        ((ProgressDialog) dialog).setMessage(info);
        ((ProgressDialog) dialog).setProgress(0);
        dialog.show();
    }

    protected void showApkUpdateDialog() {
        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new android.support.v7.app.AlertDialog.Builder(this).setCancelable(false).setTitle(R.string
                .update_sdk_title)
                .setMessage(R.string.update_sdk_message).setPositiveButton(R.string.update_sdk_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(R.string.update_sdk_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        System.exit(0);
                    }
                }).create();
        dialog.show();
    }

    protected void showUpdateFinishedDialog() {

        if (!isActivityShowing) {
            return;
        }
        if (null != dialog) {
            dialog.dismiss();
            dialog = null;
        }
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dds_resource_load_success));

        dialog = builder.create();
        dialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dialog.dismiss();
                t.cancel();
            }
        }, 2000);
    }

    @Override
    protected void onStart() {
        isActivityShowing = true;
        try {
            DDS.getInstance().getUpdater().update(ddsUpdateListener);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }

        if (DDS.getInstance() != null) {
            DDS.getInstance().getAgent().subscribe("sys.resource.updated", resourceUpdatedMessageObserver);
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        sendHiMessage();
        enableWakeIfNecessary();
        DDS.getInstance().getAgent().subscribe(new String[]{"context.output.text", "context.input.text",
                "avatar.silence", "avatar.listening", "avatar.understanding", "avatar.speaking", "context.widget.content"}, mMessageObserver);
        super.onResume();
    }

    @Override
    protected void onPause() {
        DDS.getInstance().getAgent().unSubscribe(mMessageObserver);
        disableWakeIfNecessary();
        super.onPause();
    }

    @Override
    protected void onStop() {
        isActivityShowing = false;
        AILog.d(TAG, "onStop() " + this.hashCode());
        DDS.getInstance().getAgent().unSubscribe(resourceUpdatedMessageObserver);
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
