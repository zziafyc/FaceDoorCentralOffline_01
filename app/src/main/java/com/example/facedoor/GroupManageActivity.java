package com.example.facedoor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.facedoor.base.BaseAppCompatActivity;
import com.example.facedoor.db.DBUtil;
import com.example.facedoor.db.GroupManager;
import com.example.facedoor.model.Group;
import com.example.facedoor.ui.DropEditText;
import com.example.facedoor.util.PopUpWindowUtils;
import com.example.facedoor.util.ToastShow;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GroupManageActivity extends BaseAppCompatActivity implements OnClickListener {
    private final static String TAG = GroupManageActivity.class.getSimpleName();

    private DropEditText mGroupDrop;
    private Toast mToast;
    private ProgressDialog mProDialog;
    private EditText mDBIP;
    private EditText mDoorIP;
    private EditText mDoorContoller;
    private EditText mDoorTime;
    private EditText mPlatformIP;
    private EditText mDoorNum;
    private EditText mDbAgent;
    private EditText mPersonal;
    private ImageView moreImg;
    private String groupId2;
    private EditText mVoiceValue;
    private EditText mDetectTimeValue;
    private Spinner spinner;
    private Spinner spinnerFaceDetect;
    // 身份验证对象
    private IdentityVerifier mIdVerifier;

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_group_manage;
    }

    @Override
    protected void initViewsAndEvents() {
        Button btnCreate = (Button) findViewById(R.id.btn_create);
        Button btnDelete = (Button) findViewById(R.id.btn_delete);
        Button btnAdd = (Button) findViewById(R.id.btn_add);
        btnCreate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        moreImg = (ImageView) findViewById(R.id.img_more);
        moreImg.setOnClickListener(this);
        findViewById(R.id.btn_dbip).setOnClickListener(this);
        findViewById(R.id.btn_doorip).setOnClickListener(this);
        findViewById(R.id.btn_dbAgent).setOnClickListener(this);
        findViewById(R.id.btn_doorController).setOnClickListener(this);
        findViewById(R.id.btn_openTime).setOnClickListener(this);
        findViewById(R.id.btn_platformIP).setOnClickListener(this);
        findViewById(R.id.btn_doorNum).setOnClickListener(this);
        findViewById(R.id.btn_voiceValue).setOnClickListener(this);
        findViewById(R.id.btn_detectTimeValue).setOnClickListener(this);
        findViewById(R.id.btn_personnalip).setOnClickListener(this);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] mode = getResources().getStringArray(R.array.mode);
                SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
                SharedPreferences.Editor editor = config.edit();
                editor.putInt(MyApp.FACEONLY, position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        spinnerFaceDetect = (Spinner) findViewById(R.id.spinnerFaceDetect);
        spinnerFaceDetect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] mode = getResources().getStringArray(R.array.mode);
                SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
                SharedPreferences.Editor editor = config.edit();
                editor.putInt(MyApp.FACEDETECT, position);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });


        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        String dbIP = config.getString(MyApp.DBIP_KEY, "");
        String doorip = config.getString(MyApp.DOORIP_KEY, "");
        String doorcontroller = config.getString(MyApp.DOOR_CONTROLLER, "");
        String opentime = config.getString(MyApp.OPEN_TIME, "");
        String platformip = config.getString(MyApp.PLATFORM_IP, "");
        String doornum = config.getString(MyApp.DOOR_NUM, "");
        String dbagent = config.getString(MyApp.DB_AGENT, "");
        String voiceValue = config.getString(MyApp.VOICE_VALUE, "60");
        String personal = config.getString(MyApp.PERSONNAL, "");
        String detectTimeValue = config.getString(MyApp.DETECT_TIME_VALUE, "60");
        int faceVocal = config.getInt(MyApp.FACEONLY, 0);
        int faceDetect = config.getInt(MyApp.FACEDETECT, 0);

        mGroupDrop = (DropEditText) findViewById(R.id.drop_edit);
        mDBIP = (EditText) findViewById(R.id.et_dbip);
        mDoorIP = (EditText) findViewById(R.id.et_doorip);
        mDoorContoller = (EditText) findViewById(R.id.et_doorController);
        mDoorTime = (EditText) findViewById(R.id.et_openTime);
        mPlatformIP = (EditText) findViewById(R.id.et_platformIP);
        mDoorNum = (EditText) findViewById(R.id.et_doorNum);
        mDbAgent = (EditText) findViewById(R.id.et_dbAgent);
        mPersonal = (EditText) findViewById(R.id.et_personnalip);
        mVoiceValue = (EditText) findViewById(R.id.et_voiceValue);
        mDetectTimeValue = (EditText) findViewById(R.id.et_detectTimeValue);
        spinner.setSelection(faceVocal);
        spinner.setSelection(faceDetect);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        if (dbIP != null) {
            mDBIP.setText(dbIP);
        }
        if (mDoorIP != null) {
            mDoorIP.setText(doorip);
        }
        if (mDoorContoller != null) {
            mDoorContoller.setText(doorcontroller);
        }
        if (mDoorTime != null) {
            mDoorTime.setText(opentime);
        }
        if (mPlatformIP != null) {
            mPlatformIP.setText(platformip);
        }
        if (mDoorNum != null) {
            mDoorNum.setText(doornum);
        }
        if (mDbAgent != null) {
            mDbAgent.setText(dbagent);
        }
        if (mPersonal != null) {
            mPersonal.setText(personal);
        }
        if (mVoiceValue != null) {
            mVoiceValue.setText(voiceValue);
        }
        if (mDetectTimeValue != null) {
            mDetectTimeValue.setText(detectTimeValue);
        }

        mProDialog = new ProgressDialog(this);
        // 等待框设置为不可取消
        mProDialog.setCancelable(true);
        mProDialog.setCanceledOnTouchOutside(false);
        mProDialog.setTitle("请稍候");

        mProDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // cancel进度框时,取消正在进行的操作
                if (null != mIdVerifier) {
                    mIdVerifier.cancel();
                }
            }
        });

        mIdVerifier = IdentityVerifier.createVerifier(this, new InitListener() {

            @Override
            public void onInit(int errorCode) {
                if (errorCode == ErrorCode.SUCCESS) {
                    // ToastShow.showTip(mToast, "引擎初始化成功");
                } else {
                    ToastShow.showTip(mToast, "引擎初始化失败。错误码：" + errorCode);
                }
            }
        });
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
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.btn_create:
                createGroup();
                break;
            case R.id.btn_delete:
                deleteGroup();
                break;
            case R.id.btn_add:
                addGroup();
            case R.id.btn_dbip:
                setDBIP();
                break;
            case R.id.btn_doorip:
                setDoorIP();
                break;
            case R.id.btn_doorController:
                setDoorController();
                break;
            case R.id.btn_openTime:
                setOpenTime();
                break;
            case R.id.btn_platformIP:
                setPlatformIP();
                break;
            case R.id.btn_doorNum:
                setDoorNum();
                break;
            case R.id.btn_dbAgent:
                setDbAgent();
                break;
            case R.id.btn_personnalip:
                setPersonalIP();
                break;
            case R.id.img_more:
                PopUpWindowUtils.showPop(GroupManageActivity.this, moreImg);
                break;
        }
    }

    private void setPersonalIP() {
        verifyIP(mPersonal, MyApp.PERSONNAL);
    }

    private void setDbAgent() {
        verifyIP(mDbAgent, MyApp.DB_AGENT);
    }

    private void setDoorNum() {
        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        String strDoorNum = mDoorNum.getText().toString();
        editor.putString(MyApp.DOOR_NUM, strDoorNum);
        editor.commit();
        ToastShow.showTip(mToast, "设置成功");
    }

    private void setPlatformIP() {
        verifyIP(mPlatformIP, MyApp.PLATFORM_IP);
    }

    private void setOpenTime() {
        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        String strOpenTime = mDoorTime.getText().toString();
        editor.putString(MyApp.OPEN_TIME, strOpenTime);
        editor.commit();
        ToastShow.showTip(mToast, "设置成功");
    }

    private void setDoorController() {
        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        String strDoorController = mDoorContoller.getText().toString();
        editor.putString(MyApp.DOOR_CONTROLLER, strDoorController);
        editor.commit();
        ToastShow.showTip(mToast, "设置成功");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Group> groups = MyApp.getDBManage(this).getGroups();
        BaseAdapter adp = mGroupDrop.getAdapter();
        mGroupDrop.setList(this, groups);
        if (adp != null) {
            adp.notifyDataSetChanged();
        }
        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        int faceVocal = config.getInt(MyApp.FACEONLY, 0);
        int faceDetect = config.getInt(MyApp.FACEDETECT, 0);
        spinner.setSelection(faceVocal);
        spinnerFaceDetect.setSelection(faceDetect);
    }

    /**
     * 开启进度条
     */
    private void startProgress(String msg) {
        mProDialog.setMessage(msg);
        mProDialog.show();
        findViewById(R.id.layout_group_manage).setEnabled(false);
    }

    /**
     * 关闭进度条
     */
    private void stopProgress() {
        mProDialog.dismiss();
        findViewById(R.id.layout_group_manage).setEnabled(true);
    }

    private void createGroup() {
        ArrayList<String> groupId = MyApp.getDBManage(this).getGroupId();
        if (groupId.size() != 0) {
            ToastShow.showTip(mToast, "鉴别组已建立");
            return;
        }
        final String groupName = mGroupDrop.getText();
        if (TextUtils.isEmpty(groupName)) {
            mGroupDrop.requestFocus();
            ToastShow.showTip(mToast, "组名不能为空");
            return;
        }
        startProgress("正在创建组...");
        Runnable dbOperation = new Runnable() {
            public void run() {
                final DBUtil dbUtil = new DBUtil(GroupManageActivity.this);
                final int groupId = dbUtil.getNextGroupId();
                if (groupId == -1) {
                    return;
                }
                final String groupIdStr = "" + groupId;
                GroupManager groupManager = new GroupManager(GroupManageActivity.this);
                final String responseCode = groupManager.createGroup(groupIdStr, groupName);
                if (responseCode.equals("0000")) {
                    dbUtil.addGroup(groupName);
                    // MainActivity.db.insertGroup(groupName, groupIdStr);
                    MyApp.getDBManage(GroupManageActivity.this).insertGroup(groupName, groupIdStr);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopProgress();
                        if (responseCode.equals("0000")) {
                            ToastShow.showTip(mToast, "创建组成功");
                        } else {
                            ToastShow.showTip(mToast, "创建组失败");
                        }
                    }
                });
            }
        };
        new Thread(dbOperation).start();
    }

    private void deleteGroup() {
        final String groupId = mGroupDrop.getText();
        if (TextUtils.isEmpty(groupId)) {
            ToastShow.showTip(mToast, "请选择组");
            return;
        }
        startProgress("正在删除...");
        Runnable dbOperation = new Runnable() {
            public void run() {
                GroupManager groupManager = new GroupManager(GroupManageActivity.this);
                final String responseCode = groupManager.deleteGroup(groupId);
                if (responseCode.equals("0000")) {
                    DBUtil dbUtil = new DBUtil(GroupManageActivity.this);
                    dbUtil.deleteGroup(Integer.parseInt(groupId));
                    // MainActivity.db.deleteGroup(groupId);
                    MyApp.getDBManage(GroupManageActivity.this).deleteGroup(groupId);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopProgress();
                        if (responseCode.equals("0000")) {
                            ToastShow.showTip(mToast, "删除组成功");
                        } else {
                            ToastShow.showTip(mToast, "删除组失败");
                        }
                    }
                });
            }
        };
        new Thread(dbOperation).start();
    }

    private void addGroup() {
        if (TextUtils.isEmpty(mDBIP.getText().toString())) {
            ToastShow.showTip(mToast, "sorry，请先设置数据库IP");
            return;
        }
        String groupName = mGroupDrop.getText();
        if (TextUtils.isEmpty(groupName)) {
            mGroupDrop.requestFocus();
            ToastShow.showTip(mToast, "sorry，组编号不能为空");
            return;
        }
        startProgress("正在添加组...");
        //人脸识别有结果后，执行保存图片，图片保存完之后，就进行页面的跳转
        groupId2 = mGroupDrop.getText().toString();
        Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {
                DBUtil dbUtil = new DBUtil(GroupManageActivity.this);
                String name = null;
                try {
                    name = dbUtil.queryGroup(groupId2);
                    subscriber.onNext(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(String groupName) {
                        stopProgress();
                        if (!TextUtils.isEmpty(groupName)) {
                            //插入到数据库中
                            ArrayList<String> groupIds = MyApp.getDBManage(GroupManageActivity.this).getGroupId();
                            if (groupIds != null && groupIds.size() > 0) {
                                if (groupIds.get(0).equals(groupId2)) {
                                    ToastShow.showTip(mToast, "sorry，该鉴别组已建立");
                                    return;
                                }
                            } else {
                                MyApp.getDBManage(GroupManageActivity.this).insertGroup(groupName, groupId2);
                            }
                            //刷新列表适配器
                            ArrayList<Group> groups = MyApp.getDBManage(GroupManageActivity.this).getGroups();
                            Log.e("fyc哈", groups.size() + "");
                            BaseAdapter adp = mGroupDrop.getAdapter();
                            mGroupDrop.setList(GroupManageActivity.this, groups);
                            if (adp != null) {
                                adp.notifyDataSetChanged();
                            }
                        } else {
                            ToastShow.showTip(GroupManageActivity.this, "sorry，该组不存在");
                        }
                    }
                });
    }

    private void setDBIP() {
        String regEx = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(regEx);
        String dbIP = mDBIP.getText().toString();
        Matcher matcher = pattern.matcher(dbIP);
        boolean rs = matcher.matches();
        System.out.println(dbIP);
        System.out.println(rs);
        if (rs == false) {
            ToastShow.showTip(mToast, "IP地址不正确");
            return;
        }
        Observable.just(dbIP).map(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String arg0) {
                DBUtil dbUtil = new DBUtil(arg0);
                return dbUtil.testConnection();
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean connected) {
                        if (connected) {
                            SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
                            SharedPreferences.Editor editor = config.edit();
                            editor.putString(MyApp.DBIP_KEY, mDBIP.getText().toString());
                            editor.commit();
                            ToastShow.showTip(mToast, "设置成功");
                        } else {
                            ToastShow.showTip(mToast, "无效的IP地址");
                        }
                    }
                });
    }

    private void setDoorIP() {
        verifyIP(mDoorIP, MyApp.DOORIP_KEY);
    }

    private void verifyIP(final EditText ip, final String key) {
        String regEx = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(regEx);
        String strIP = ip.getText().toString();
        Matcher matcher = pattern.matcher(strIP);
        boolean rs = matcher.matches();
        System.out.println(strIP);
        System.out.println(rs);
        if (rs == false) {
            ToastShow.showTip(mToast, "IP地址不正确");
            return;
        }

        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putString(key, ip.getText().toString());
        editor.commit();
        ToastShow.showTip(mToast, "设置成功");

    }

    protected void onDestroy() {
        super.onDestroy();
        if (null != mIdVerifier) {
            mIdVerifier.destroy();
            mIdVerifier = null;
        }
    }
}
