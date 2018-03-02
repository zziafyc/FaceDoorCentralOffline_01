package com.example.facedoor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.facedoor.db.DBUtil;
import com.example.facedoor.door.DBPorxyComm;
import com.example.facedoor.door.DoorJH;
import com.example.facedoor.door.Openable;
import com.example.facedoor.door.PlatformComm;
import com.example.facedoor.util.DemoConstant;
import com.example.facedoor.util.ImageFile;
import com.example.facedoor.util.PopUpWindowUtils;
import com.example.facedoor.util.ProgressShow;
import com.example.facedoor.util.ToastShow;
import com.example.facedoor.util.TonePlayer;
import com.iflytek.aipsdk.auth.Auth;
import com.iflytek.aipsdk.auth.IAuthListener;
import com.iflytek.aipsdk.param.HashParam;
import com.iflytek.cloud.record.PcmRecorder;
import com.iflytek.msc.JniSpeex;
import com.iflytek.msc.JniSpeexOut;
import com.iflytek.util.IflyRecorder;
import com.iflytek.util.IflyRecorderListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class IdentifyActivity extends Activity {

    private static final String TAG = IdentifyActivity.class.getSimpleName();
    @SuppressLint("SdCardPath")
    private static final String CROP_FACE_PATH = "/mnt/sdcard/FaceVocal/crop.jpg";

    private Toast mToast;
    private ProgressDialog mProDialog;
    private View mLayout;
    private TextView mScore;
    private byte[] mImageData;
    private String session_id;
    private String pwd = "";
    private byte[] audioBuff = new byte[10 * 1024 * 1024];
    private int audioLen;
    private ImageView moreImg;

    private String mGroupId;
    private TextView mResultEditText;
    private PcmRecorder mPcmRecorder;
    private String authId;
    private TextView name;
    private TextView num;
    // 声纹验证通过时间
    private static final int FACE_SUCCESS = 3000;
    // 声纹验证失败时间
    private static final int FACE_FAILED = 2000;
    // 人脸声纹鉴别成功
    // 已经创建组，组里面有成员。未注册的人进行检测
    private static final int NO_REGISTER_DERECTION = 2000;
    //声纹鉴别失败
    private static final int VOICE_FAILED = 2000;
    // 没有人脸时间
    private static final int NOT_FOUND_IN_DB = 1000;

    private Auth mAuth;
    private Intent mIntent;
    private static final String PARAMS = "appid=" + MyApp.APPID + ",aue=raw,operationType=identify,topN=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);
        mLayout = findViewById(R.id.layout_identify);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mScore = (TextView) findViewById(R.id.id_score);
        mResultEditText = (TextView) findViewById(R.id.vocal_edit_result);
        moreImg = (ImageView) findViewById(R.id.img_more);
        moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpWindowUtils.showPop(IdentifyActivity.this, moreImg);
            }
        });

        initUI();
        getPhoto();

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍候");
        // cancel进度框时，取消正在进行的操作
        mProDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        mImageData = ImageFile.readImageFromFile("crop.jpg");
        mIntent = new Intent(IdentifyActivity.this, VideoDetect.class);

    }

    private void getPhoto() {
        ImageView imageView = (ImageView) findViewById(R.id.image);

        File file = new File(CROP_FACE_PATH);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(CROP_FACE_PATH);
            imageView.setImageBitmap(bitmap);
        }
    }

    private void initUI() {
        IflyRecorder.getInstance().initRecoder(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, MediaRecorder.AudioSource.MIC);
        name = (TextView) findViewById(R.id.name);
        num = (TextView) findViewById(R.id.num);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        String personal = config.getString(MyApp.PERSONNAL, "");
        String dbip = config.getString(MyApp.DBIP_KEY, "");
        String doorip = config.getString(MyApp.DOORIP_KEY, "");
        String platformip = config.getString(MyApp.PLATFORM_IP, "");
        String dbagent = config.getString(MyApp.DB_AGENT, "");
        if (personal.equals("")) {
            new AlertDialog.Builder(IdentifyActivity.this).setTitle("请设置私有云IP")
                    .setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(IdentifyActivity.this, GroupManageActivity.class);
                            startActivity(intent);
                        }
                    }).show();
            return;
        }
        if (dbip.equals("")) {
            new AlertDialog.Builder(IdentifyActivity.this).setTitle("请设置数据库IP")
                    .setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(IdentifyActivity.this, GroupManageActivity.class);
                            startActivity(intent);
                        }
                    }).show();
            return;
        }
        if (doorip.equals("")) {
            new AlertDialog.Builder(IdentifyActivity.this).setTitle("请设置门禁服务器IP")
                    .setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(IdentifyActivity.this, GroupManageActivity.class);
                            startActivity(intent);
                        }
                    }).show();
            return;
        }
        if (platformip.equals("")) {
            new AlertDialog.Builder(IdentifyActivity.this).setTitle("请设置平台IP")
                    .setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(IdentifyActivity.this, GroupManageActivity.class);
                            startActivity(intent);
                        }
                    }).show();
            return;
        }
        if (dbagent.equals("")) {
            new AlertDialog.Builder(IdentifyActivity.this).setTitle("请设置数据库代理IP")
                    .setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(IdentifyActivity.this, GroupManageActivity.class);
                            startActivity(intent);
                        }
                    }).show();
            return;
        }
        mAuth = new Auth();
        mAuth.init(personal + ":8080", MyApp.APPID, 5);
        identify();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void identify() {
        if (mImageData == null) {
            ToastShow.showTip(mToast, "请选择图片后再验证");
            return;
        }
        // Log.e("IdentifyActivity@identify", MainActivity.db ==
        // null?"null":"not null");
        // ArrayList<String> groupIdList = MainActivity.db.getGroupId();
        ArrayList<String> groupIdList = MyApp.getDBManage(this).getGroupId();
        mGroupId = null;
        if (groupIdList != null && groupIdList.size() != 0) {
            mGroupId = groupIdList.get(0);
        }
        if (mGroupId == null) {
            ToastShow.showTip(mToast, "本机未建立组，请先建立组");
            delayedFinish(2000);
            return;
        }
        ProgressShow.show(mProDialog, "鉴别中。。。", mLayout);
        String params = PARAMS + ",groupId=" + mGroupId;
        mAuth.faceRecognize(params, mImageData, identifyListener);
    }

    private int AuthIdToUserId(String authId) {
        return Integer.parseInt(authId.substring(1));
    }

    private void delayedFinish(final int mills) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(mills);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        }.start();
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth != null) {
            mAuth.uninit();
        }
        if (null != mPcmRecorder) {
            mPcmRecorder.stopRecord(true);
        }
    }

    private void faceOnlySuceess() {
        int userId = AuthIdToUserId(authId);
        DBUtil dbUtil = new DBUtil(IdentifyActivity.this);
        String userName = dbUtil.queryStaffIdAndName(userId);
        if (userName != null) {
            String staffID = userName.split("[;]")[0];
            String doorID = IdentifyActivity.this.getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE).getString(MyApp.DOOR_NUM, "");
            File image = new File(CROP_FACE_PATH);
            DBPorxyComm dbPorxyComm = new DBPorxyComm(IdentifyActivity.this);
            dbPorxyComm.sendNormalMessage(staffID, image);
            String[] str = userName.split("[;]");
            final String num1 = str[0];
            final String name1 = str[1];
            Openable door = new DoorJH(IdentifyActivity.this);
            door.open();
            if (!TextUtils.isEmpty(door.getExceptionShow())) {
                ToastShow.showTip(mToast, door.getExceptionShow());

            }
            TonePlayer.playTone(IdentifyActivity.this, DemoConstant.SUCCESS, null);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mResultEditText.setText("验证通过");
                    ToastShow.showTip(mToast, "你好" + name1);
                    num.setText("工号： " + num1);
                    name.setText("姓名 : " + name1);
                    delayedFinish(FACE_SUCCESS);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastShow.showTip(mToast, "数据库无此人");
                    delayedFinish(NOT_FOUND_IN_DB);
                }
            });
        }
    }

    private void faceVocalSuceess() {

        String REC_PARAMS_BASE = "uid=" + authId + ","
                + "appid=pc20onli,platform=Android,aue=speex-wb,featureType=voice,rgn=5,auf=audio/L16;rate=16000,sub=ivp,ssm=0,work_mode=digit_mode";
        System.out.println(authId);
        VP_RECOGNIZE_PARAMS_VERIFY = REC_PARAMS_BASE + ",operationType=verifyInit";
        mAuth.vpInit(VP_RECOGNIZE_PARAMS_VERIFY, authListener);
    }

    private IAuthListener identifyListener = new IAuthListener() {

        @Override
        public void onFaceResult(final String arg0, int arg1, long arg2, String arg3) {
            if (null != mProDialog) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressShow.stop(mProDialog, mLayout);
                    }
                });
            }

            System.out.println(arg0);
            try {
                JSONObject resultJson = new JSONObject(arg0);
                String responseCode = (String) resultJson.get("responseCode");
                if ("0000".equals(responseCode)) {
                    JSONObject candidateOne = resultJson.getJSONArray("users").getJSONObject(0);
                    if (candidateOne == null) {
                        return;
                    }
                    final double score = candidateOne.getDouble("score");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mScore.setText("" + score);
                        }
                    });

                    if (score > 91) {
                        authId = candidateOne.getString("uid");
                        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
                        int faceVocal = config.getInt(MyApp.FACEONLY, 0);
                        if (faceVocal == 1) {
                            faceOnlySuceess();
                        } else {
                            faceVocalSuceess();
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mResultEditText.setText("对不起，请注册");
                            }
                        });
                        TonePlayer.playTone(IdentifyActivity.this, DemoConstant.REGISTER, null);
                        identifyFailedUploadImages("B");
                        delayedFinish(NO_REGISTER_DERECTION);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText("对不起，鉴别失败");
                        }
                    });
                    TonePlayer.playTone(IdentifyActivity.this, DemoConstant.REGISTER_FAILED, null);
                    identifyFailedUploadImages("C");
                    delayedFinish(FACE_FAILED);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFusionResult(String arg0, int arg1, long arg2, String arg3) {

        }

        @Override
        public void onVoiceprintResult(String arg0, int arg1, long arg2, String arg3) {

        }

    };
    private IAuthListener authListener = new IAuthListener() {

        @Override
        public void onVoiceprintResult(String result, int i, long il, String sl) {
            System.out.println("result：" + result);
            System.out.println("i:" + i);
            System.out.println("il:" + il);
            System.out.println("sl:" + sl);
            if (i == 0) {
                if ("verifyInit".equals(sl)) {
                    String numPwd = "";
                    try {
                        JSONObject jsonObject = new JSONObject(result.trim());
                        numPwd = (String) jsonObject.get("ptxt");
                        session_id = (String) jsonObject.get("sessionId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    pwd = numPwd;
                    final String msg = numPwd + "";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText(pwd);
                            ToastShow.showTip(mToast, "录音机打开");
                        }
                    });
                    openRecoder();
                } else if ("verify".equals(sl)) {
                    String responseCode = "";
                    double score = 0;
                    try {
                        JSONObject jsonObject = new JSONObject(result.trim());
                        responseCode = jsonObject.getString("responseCode");
                        score = jsonObject.getDouble("score");
                    } catch (JSONException e) {
                        Log.e("", e.getMessage());
                    }
                    if ("0000".equals(responseCode) && score >= 88) {
                        int userId = AuthIdToUserId(authId);
                        DBUtil dbUtil = new DBUtil(IdentifyActivity.this);
                        String userName = dbUtil.queryStaffIdAndName(userId);
                        if (userName != null) {
                            String staffID = userName.split("[;]")[0];
                            String doorID = IdentifyActivity.this.getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE)
                                    .getString(MyApp.DOOR_NUM, "");
                            File image = new File(CROP_FACE_PATH);
                            DBPorxyComm dbPorxyComm = new DBPorxyComm(IdentifyActivity.this);
                            dbPorxyComm.sendNormalMessage(staffID, image);
                            String[] str = userName.split("[;]");
                            final String num1 = str[0];
                            final String name1 = str[1];
                            Openable door = new DoorJH(IdentifyActivity.this);
                            door.open();
                            TonePlayer.playTone(IdentifyActivity.this, DemoConstant.SUCCESS, null);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultEditText.setText("验证通过");
                                    ToastShow.showTip(mToast, "你好" + name1);
                                    num.setText("工号： " + num1);
                                    name.setText("姓名 : " + name1);
                                    delayedFinish(FACE_SUCCESS);
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mResultEditText.setText("声纹不一致");
                                ToastShow.showTip(mToast, "声纹不一致");
                            }
                        });
                        identifyFailedUploadImages("D");
                        delayedFinish(VOICE_FAILED);
                    }
                }
            }
            if (i == -1) {
                identifyFailedUploadImages("D");
                String responseCode = "";
                try {
                    JSONObject jsonObject = new JSONObject(result.trim());
                    responseCode = jsonObject.getString("responseCode");
                } catch (JSONException e) {
                    Log.e("", e.getMessage());
                }
                if ("20103".equals(responseCode)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText("声音太大");
                            delayedFinish(VOICE_FAILED);
                        }
                    });
                } else if ("20104".equals(responseCode)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText("信噪比低");
                            delayedFinish(VOICE_FAILED);
                        }
                    });
                } else if ("20110".equals(responseCode)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText("音频数量不足");
                            delayedFinish(VOICE_FAILED);
                        }
                    });
                } else if ("20111".equals(responseCode)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText("音频数量超过上限");
                            delayedFinish(VOICE_FAILED);
                        }
                    });
                } else if ("20112".equals(responseCode)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText("文本不匹配，音频内容与文本不一致");
                            delayedFinish(VOICE_FAILED);
                        }
                    });
                } else if ("20117".equals(responseCode)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText("声音太小");
                            delayedFinish(VOICE_FAILED);
                        }
                    });
                }
            }
        }

        @Override
        public void onFusionResult(String arg0, int arg1, long arg2, String arg3) {

        }

        @Override
        public void onFaceResult(String arg0, int arg1, long arg2, String arg3) {

        }
    };

    private void openRecoder() {
        audioLen = 0;
        String params = "";
        params += "sessionId=" + session_id;
        params += ",ptxt=" + pwd;
        params += ",operationType=verify";

        HashParam hashParam = new HashParam();
        hashParam.putMultiParam(VP_RECOGNIZE_PARAMS_VERIFY);
        hashParam.putMultiParam(params);
        string = hashParam.toString();
        IflyRecorder.getInstance().startRecoder(iflyRecorderListener);
        try {
            Thread.sleep(5000);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastShow.showTip(mToast, "录音机关闭");
                }
            });
            IflyRecorder.getInstance().stopRecorder();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] audio = new byte[audioLen];
            Log.e("ddddddd", "音频长度 " + audioLen);
            System.arraycopy(audioBuff, 0, audio, 0, audioLen);
            if (audioLen > 320000) {
                showTip("录音过长");
            }
            int encode_ret = audioEncode(audio);
            if (encode_ret != 0) {
                showTip("音频压缩异常");
            }
            if (TextUtils.isEmpty(session_id)) {
                showTip("声纹会话异常");
            }
            mAuth.vpRecognize(string, out.speexdata, authListener);
            System.out.println(string);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private IflyRecorderListener iflyRecorderListener = new IflyRecorderListener() {

        @Override
        public void OnReceiveBytes(byte[] arg0, int arg1) {
            System.arraycopy(arg0, 0, audioBuff, audioLen, arg0.length);
            audioLen += arg0.length;
            System.out.println(arg0);
            System.out.println(arg1);
        }
    };
    private JniSpeexOut out = new JniSpeexOut();
    private String string;
    private String VP_RECOGNIZE_PARAMS_VERIFY;

    private int audioEncode(byte[] buf) {
        // 初始化编码器，选择窄带算法(对应8K采用率音频)
        int ret = JniSpeex.EncodeInit(1, out);
        if (ret != 0)
            return -1;

        byte[] data = new byte[buf.length];
        System.arraycopy(buf, 0, data, 0, buf.length);

        // 音频编码,压缩比约为5.78:1
        ret = JniSpeex.Encode(out.handle, data, data.length, data.length, (short) 10, out);

        if (ret != 0)
            return -1;

        // Speex编码逆初始化
        JniSpeex.EncodeFini(out.handle);
        return 0;
    }

    private void identifyFailedUploadImages(String type) {
        PlatformComm platformComm = new PlatformComm(IdentifyActivity.this);
        platformComm.sendAbnormalMessage(type, new File(CROP_FACE_PATH));
        DBPorxyComm dbPorxyComm = new DBPorxyComm(IdentifyActivity.this);
        dbPorxyComm.sendAbnormalMessage(type, new File(CROP_FACE_PATH));
    }
}
