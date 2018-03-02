package com.example.facedoor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.facedoor.util.PopUpWindowUtils;
import com.example.facedoor.util.ToastShow;
import com.iflytek.aipsdk.auth.Auth;
import com.iflytek.aipsdk.auth.IAuthListener;
import com.iflytek.aipsdk.param.HashParam;
import com.iflytek.msc.JniSpeex;
import com.iflytek.msc.JniSpeexOut;
import com.iflytek.util.IflyRecorder;
import com.iflytek.util.IflyRecorderListener;

import org.json.JSONException;
import org.json.JSONObject;

public class VocalVerifyActivity extends Activity implements OnClickListener {
    private static final String TAG = VocalVerifyActivity.class.getSimpleName();

    private String session_id;
    private String[] mNumPword;
    private int sucTimes = 1;
    private int audioLen;
    private byte[] audioBuff = new byte[10 * 1024 * 1024];
    private String pwd = "";
    // 用户id，唯一标识
    private String Users_Id;

    // // 数字声纹密码
    // private String mNumPwd = "";
    // 数字声纹密码段，默认有5段
    private String[] mNumPwdSegs;
    // 用于验证的数字密码
    private String mVerifyNumPwd = "";
    // UI控件
    private TextView mResultEditText;
    private RadioGroup mSstTypeGroup;
    private Auth mAuth;

    private AlertDialog mTextPwdSelectDialog;
    private Toast mToast;
    private String REC_PARAMS_BASE = "appid=pc20onli,platform=Android,aue=speex-wb,featureType=voice,rgn=5,auf=audio/L16;rate=16000,sub=ivp,ssm=0,work_mode=digit_mode";
    private String param = REC_PARAMS_BASE + ",operationType=regInit";
    private String VP_RECOGNIZE_PARAMS_LOGOUT = REC_PARAMS_BASE + ",operationType=logout";
    private String VP_RECOGNIZE_PARAMS_VERIFY = REC_PARAMS_BASE + ",operationType=verifyInit";

    private ProgressDialog mProDialog;
    private Button mPressToTalkButton;
    private ImageView moreImg;

    /**
     * 按压监听器
     */
    private OnTouchListener mPressTouch = new OnTouchListener() {

        private String string;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    audioLen = 0;
                    String params = "";
                    params += "sessionId=" + session_id;
                    if (mNumPword != null && mNumPword.length > 0) {
                        params += ",ptxt=" + mNumPword[sucTimes - 1];
                        params += ",operationType=reg";
                        if (sucTimes == 5) {
                            param += ",audio_status=4";
                        }
                    }
                    HashParam hashParam = new HashParam();
                    hashParam.putMultiParam(param);
                    hashParam.putMultiParam(params);
                    string = hashParam.toString();
                    IflyRecorder.getInstance().startRecoder(iflyRecorderListener);
                    break;

                case MotionEvent.ACTION_UP:
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
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocal_demo);

        // 获取用户从人脸识别界面注册的id
        Intent intent = getIntent();
        Users_Id = intent.getStringExtra("ID");
        REC_PARAMS_BASE = "uid=" + Users_Id + "," + REC_PARAMS_BASE;
        param = REC_PARAMS_BASE + ",operationType=regInit";
        VP_RECOGNIZE_PARAMS_LOGOUT = REC_PARAMS_BASE + ",operationType=logout";
        VP_RECOGNIZE_PARAMS_VERIFY = REC_PARAMS_BASE + ",operationType=verifyInit";
        initUI();
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

    private void initUI() {
        IflyRecorder.getInstance().initRecoder(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, MediaRecorder.AudioSource.MIC);

        moreImg = (ImageView) findViewById(R.id.img_more);
        moreImg.setOnClickListener(this);
        mResultEditText = (TextView) findViewById(R.id.vocal_edt_result);
        mtalk = (Button) findViewById(R.id.press_to_talk);
        mtalk.setOnTouchListener(mPressTouch);

        mProDialog = new ProgressDialog(VocalVerifyActivity.this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍候");
        // cancel进度框时，取消正在进行的操作
        mProDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        mToast = Toast.makeText(VocalVerifyActivity.this, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onClick(View v) {
        // 取消先前操作

        switch (v.getId()) {
            case R.id.img_more:
                PopUpWindowUtils.showPop(VocalVerifyActivity.this, moreImg);
                break;

            default:
                break;
        }
    }

    @Override
    public void finish() {
        if (null != mTextPwdSelectDialog) {
            mTextPwdSelectDialog.dismiss();
        }

        setResult(RESULT_OK);
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.uninit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        String personal = config.getString(MyApp.PERSONNAL, "");
        if (personal.equals("")) {
            ToastShow.showTip(mToast, "请设置私有云IP");
        }
        mAuth = new Auth();
        mAuth.init(personal + ":8080", MyApp.APPID, 5);
        long sid = mAuth.vpInit(param, authListener);
        // mAuth.vpInit(VP_RECOGNIZE_PARAMS_VERIFY, authListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    private IAuthListener authListener = new IAuthListener() {

        @Override
        public void onVoiceprintResult(String result, int i, long il, String sl) {
            System.out.println("result：" + result);
            System.out.println("i:" + i);
            System.out.println("il:" + il);
            System.out.println("sl:" + sl);
            if (i == 0) {
                if ("regInit".equals(sl)) {
                    String numPwd = "";
                    try {
                        JSONObject jsonObject = new JSONObject(result.trim());
                        numPwd = (String) jsonObject.get("ptxt");
                        session_id = (String) jsonObject.get("sessionId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mNumPword = numPwd.split("-");
                    int leftTimes = 0;
                    if (sucTimes - 1 < mNumPword.length) {
                        final String msg = mNumPword[sucTimes - 1] + "\n训练 第" + sucTimes + "遍，剩余" + leftTimes + "遍";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mResultEditText.setText(msg);
                            }
                        });
                    }
                } else if ("reg".equals(sl)) {
                    String responseCode = "";
                    try {
                        JSONObject jsonObject = new JSONObject(result.trim());
                        responseCode = (String) jsonObject.get("responseCode");
                    } catch (JSONException e) {
                        Log.e("", e.getMessage());
                    }
                    if ("0000".equals(responseCode)) {
                        sucTimes++;
                        int leftTimes = 5 - sucTimes;
                        if (sucTimes - 1 < mNumPword.length) {
                            final String msg = mNumPword[sucTimes - 1] + "\n训练 第" + sucTimes + "遍，剩余" + leftTimes + "遍";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultEditText.setText(msg);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultEditText.setText("注册完成");
                                    finish();
                                }
                            });
                        }
                    }
                } else if ("logout".equals(sl)) {
                    String responseCode = "";
                    try {
                        JSONObject jsonObject = new JSONObject(result.trim());
                        responseCode = (String) jsonObject.get("responseCode");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if ("0000".equals(responseCode)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mResultEditText.setText("删除成功");
                            }
                        });
                    }
                } else if ("verifyInit".equals(sl)) {
                    System.out.println("111111111");
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
                    System.out.println(numPwd);
                    System.out.println(msg);
                    System.out.println(pwd);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultEditText.setText(pwd);
                        }
                    });
                } else if ("verify".equals(sl)) {
                    String responseCode = "";
                    try {
                        JSONObject jsonObject = new JSONObject(result.trim());
                        responseCode = (String) jsonObject.get("responseCode");
                    } catch (JSONException e) {
                        Log.e("", e.getMessage());
                    }
                    if ("0000".equals(responseCode)) {
                        String msg = "done";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mResultEditText.setText("验证通过");
                            }
                        });
                    }
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
    private IflyRecorderListener iflyRecorderListener = new IflyRecorderListener() {

        @Override
        public void OnReceiveBytes(byte[] arg0, int arg1) {
            System.arraycopy(arg0, 0, audioBuff, audioLen, arg0.length);
            audioLen += arg0.length;
            System.out.println(arg0);
            System.out.println(arg1);
        }
    };

    private Button mtalk;
    private JniSpeexOut out = new JniSpeexOut();

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

    private OnTouchListener mPressReg = new OnTouchListener() {

        private String string;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
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
                    break;
                case MotionEvent.ACTION_UP:
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
                    break;
            }

            return false;
        }

    };

}
