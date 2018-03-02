package com.example.facedoor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.facedoor.db.DBUtil;
import com.example.facedoor.db.GroupManager;
import com.example.facedoor.model.Group;
import com.example.facedoor.util.PopUpWindowUtils;
import com.example.facedoor.util.ProgressShow;
import com.example.facedoor.util.ToastShow;
import com.iflytek.aipsdk.auth.Auth;
import com.iflytek.aipsdk.auth.IAuthListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends Activity implements OnClickListener {
    private static final int REQUEST_GROUP_CHOOSE = 88;
    private final static String TAG = RegisterActivity.class.getSimpleName();
    // 选择图片后返回
    public static final int REQUEST_PICK_PICTURE = 1;
    // 拍照后返回
    private final static int REQUEST_CAMERA_IMAGE = 2;
    // 裁剪图片成功后返回
    public static final int REQUEST_INTENT_CROP = 3;
    // 删除模型
    private Toast mToast;
    private ProgressDialog mProDialog;
    private LinearLayout mGroups;
    private View mLayout;
    private ImageView moreImg;

    private File mPictureFile;
    private byte[] mImageData;
    private Bitmap mImageBitmap = null;

    private volatile String mAuthId;
    private String mGroupId;
    private HashMap<String, String> mName2ID = new HashMap<String, String>();
    private ArrayList<String> mGroupJoin = new ArrayList<String>();
    private volatile boolean mIsStaffExist;
    private List<Group> choosedGroups = new ArrayList<>();
    private TextView chooseTv;

    private Auth mAuth;
    private static final String PARAMS = "appid=" + MyApp.APPID + ",aue=raw";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnReg = (Button) findViewById(R.id.online_reg);
        Button btnDelete = (Button) findViewById(R.id.online_delete);
        moreImg = (ImageView) findViewById(R.id.img_more);
        moreImg.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        findViewById(R.id.online_pick).setOnClickListener(this);
        findViewById(R.id.online_camera).setOnClickListener(this);
        chooseTv = (TextView) findViewById(R.id.groupChoose);
        chooseTv.setOnClickListener(this);

        SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
        String dbIP = config.getString(MyApp.DBIP_KEY, "");
        if (TextUtils.isEmpty(dbIP)) {
            btnReg.setEnabled(false);
            btnDelete.setEnabled(false);
        }

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mGroups = (LinearLayout) findViewById(R.id.online_groups);
        mLayout = findViewById(R.id.register_layout);
        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍候");
        // cancel进度框时，取消正在进行的操作
        mProDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });


        mToast = Toast.makeText(RegisterActivity.this, "", Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

        // do not put it in onResume(), cropPicture() cause quickly switch from
        // onResume() to onPause()
        // at that time, mName2ID and mGroups are still empty in onPause()
        /*Observable.create(new OnSubscribe<ArrayList<String>>() {
            @Override
            public void call(Subscriber<? super ArrayList<String>> arg0) {
                DBUtil dbUtil = new DBUtil(RegisterActivity.this);
                ArrayList<String> id = new ArrayList<String>();
                ArrayList<String> name = new ArrayList<String>();
                dbUtil.queryGroups(id, name);
                int length = id.size();
                for (int i = 0; i < length; i++) {
                    mName2ID.put(name.get(i), id.get(i));
                }
                arg0.onNext(name);
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<String>>() {
                    @Override
                    public void call(ArrayList<String> name) {
                        int length = name.size();
                        for (int i = 0; i < length; i++) {
                            CheckBox checkBox = new CheckBox(RegisterActivity.this);
                            checkBox.setText(name.get(i));
                            mGroups.addView(checkBox);
                        }
                    }
                });*/
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
        if (personal.equals("")) {
            new AlertDialog.Builder(RegisterActivity.this).setTitle("请设置私有云IP")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(RegisterActivity.this, GroupManageActivity.class);
                            startActivity(intent);
                        }
                    }).show();
            return;
        }
        if (dbip.equals("")) {
            new AlertDialog.Builder(RegisterActivity.this).setTitle("请设置数据库IP")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(RegisterActivity.this, GroupManageActivity.class);
                            startActivity(intent);
                        }
                    }).show();
            return;
        }
        mAuth = new Auth();
        mAuth.init(personal + ":8080", MyApp.APPID, 5);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth != null) {
            mAuth.uninit();
        }
        mAuth = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mName2ID.clear();
        mGroups.removeAllViews();
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.groupChoose:
                Intent intentChoose = new Intent(this, ChooseGroupActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("choosedGroups", (Serializable) choosedGroups);
                intentChoose.putExtras(bundle);
                startActivityForResult(intentChoose, REQUEST_GROUP_CHOOSE);
                break;
            case R.id.online_pick:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_PICTURE);
                break;
            case R.id.online_camera:
                // 设置相机拍照后照片保存路径
                mPictureFile = new File(Environment.getExternalStorageDirectory(),
                        "picture" + System.currentTimeMillis() / 1000 + ".jpg");
                // 启动拍照,并保存到临时文件
                Intent mIntent = new Intent();
                mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPictureFile));
                mIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                startActivityForResult(mIntent, REQUEST_CAMERA_IMAGE);
                break;
            case R.id.online_reg:
                // 人脸注册
                register();
                break;
            case R.id.online_delete:
                executeModelCommand("delete");
                break;
            case R.id.img_more:
                PopUpWindowUtils.showPop(RegisterActivity.this, moreImg);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        String fileSrc = null;
        if (requestCode == REQUEST_PICK_PICTURE) {
            if ("file".equals(data.getData().getScheme())) {
                // 有些低版本机型返回的Uri模式为file
                fileSrc = data.getData().getPath();
            } else {
                // Uri模型为content
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(data.getData(), proj, null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                fileSrc = cursor.getString(idx);
                cursor.close();
            }
            // 跳转到图片裁剪页面
            cropPicture(this, Uri.fromFile(new File(fileSrc)));
        } else if (requestCode == REQUEST_CAMERA_IMAGE) {
            if (null == mPictureFile) {
                ToastShow.showTip(mToast, "拍照失败，请重试");
                return;
            }

            fileSrc = mPictureFile.getAbsolutePath();
            updateGallery(fileSrc);
            // 跳转到图片裁剪页面,需要先进行图片镜像翻转
            Bitmap bitmap = BitmapFactory.decodeFile(fileSrc);
            bitmap = flipBitmap(bitmap);
            File file = new File(getImagePath2());//将要保存图片的路径
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            cropPicture(this, Uri.fromFile(new File(getImagePath2())));


        } else if (requestCode == REQUEST_INTENT_CROP) {
            // 获取返回数据
            Bitmap bmp = data.getParcelableExtra("data");
            // 获取裁剪后图片保存路径
            fileSrc = getImagePath();

            // 若返回数据不为null，保存至本地，防止裁剪时未能正常保存
            if (null != bmp) {
                saveBitmapToFile(bmp);
            }  // 获取图片的宽和高
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            mImageBitmap = BitmapFactory.decodeFile(fileSrc, options);

            // 压缩图片
            options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(
                    (double) options.outWidth / 1024f,
                    (double) options.outHeight / 1024f)));
            options.inJustDecodeBounds = false;
            mImageBitmap = BitmapFactory.decodeFile(fileSrc, options);

            // 若mImageBitmap为空则图片信息不能正常获取
            if (null == mImageBitmap) {
                //ToastShow.showTip(RegisterActivity.this, "图片信息无法正常获取！");
                return;
            }
            // 部分手机会对图片做旋转，这里检测旋转角度
            int degree = readPictureDegree(fileSrc);
            if (degree != 0) {
                // 把图片旋转为正的方向
                mImageBitmap = rotateImage(degree, mImageBitmap);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 可根据流量及网络状况对图片进行压缩
            mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            mImageData = baos.toByteArray();
            ((ImageView) findViewById(R.id.online_img)).setImageBitmap(mImageBitmap);
        } else if (requestCode == REQUEST_GROUP_CHOOSE) {
            if (data != null) {
                choosedGroups = (List<Group>) data.getSerializableExtra("choosedGroups");
                if (choosedGroups != null) {
                    chooseTv.setText("已选择" + choosedGroups.size() + "个组,点击选择？");
                    if (choosedGroups.size() > 0) {
                        for (Group group : choosedGroups) {
                            mGroupJoin.add(group.getId());
                        }
                    }
                }
            }
        }
    }

    private void register() {
        final String staffName = ((EditText) findViewById(R.id.online_name)).getText().toString();
        if (TextUtils.isEmpty(staffName)) {
            ToastShow.showTip(mToast, "用户名不能为空");
            return;
        }
        final String staffID = ((EditText) findViewById(R.id.online_number)).getText().toString();
        if (TextUtils.isEmpty(staffID)) {
            ToastShow.showTip(mToast, "工号不能为空");
            return;
        }
        String regEx = "[A-Z][0-9]{5}";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(staffID);
        boolean rs = matcher.matches();
        if (rs == false) {
            ToastShow.showTip(mToast, "工号格式不正确");
            return;
        }
        mIsStaffExist = true;
        Runnable queryStaffID = new Runnable() {
            public void run() {
                DBUtil dbUtil = new DBUtil(RegisterActivity.this);
                try {
                    mIsStaffExist = dbUtil.isStaffExist(staffID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread queryThread = new Thread(queryStaffID);
        queryThread.start();
        try {
            queryThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mIsStaffExist) {
            ToastShow.showTip(mToast, "用户已存在");
            return;
        }

       /* for (int i = 0; i < mGroups.getChildCount(); i++) {
            CheckBox child = (CheckBox) mGroups.getChildAt(i);
            if (child != null && child.isChecked()) {
                String groupName = child.getText().toString();
                mGroupJoin.add(mName2ID.get(groupName));
            }
        }*/
        if (mGroupJoin.size() == 0) {
            ToastShow.showTip(mToast, "请勾选组");
            return;
        }
        if (mImageData == null) {
            ToastShow.showTip(mToast, "请选择图片后再注册");
            return;
        }

        ProgressShow.show(mProDialog, "注册中...", mLayout);

        Runnable register = new Runnable() {
            public void run() {
                DBUtil dbUtil = new DBUtil(RegisterActivity.this);
                int userID = dbUtil.getNextUserId();
                Log.e("RegisterActivity", "next userId:" + userID);
                if (userID == -1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastShow.showTip(mToast, "无法从数据库获取下一个UserId！");
                        }
                    });
                    return;
                }
                // Z00001
                // mAuthId = "a" + userId;
                mAuthId = userIdToAuthId(userID);
                String params = PARAMS + ",uid=" + mAuthId + ",operationType=reg";
                mAuth.faceRecognize(params, mImageData, faceRegListener);
            }
        };
        new Thread(register).start();

    }

    private void executeModelCommand(final String cmd) {
        final String staffID = ((EditText) findViewById(R.id.online_number)).getText().toString();
        if (TextUtils.isEmpty(staffID)) {
            ToastShow.showTip(mToast, "工号不能为空");
            return;
        }
        Runnable delete = new Runnable() {
            public void run() {
                DBUtil dbUtil = new DBUtil(RegisterActivity.this);
                int userID = dbUtil.queryUserID(staffID);
                if (userID == -1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastShow.showTip(mToast, "用户不存在");
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressShow.show(mProDialog, "删除中。。。", mLayout);
                    }
                });

                mAuthId = userIdToAuthId(userID);
                ArrayList<String> groupIDs = dbUtil.queryUserGroups(userID);
                GroupManager groupManager = new GroupManager(RegisterActivity.this);
                List<String> fail = groupManager.quitGroup(mAuthId, groupIDs);
                if (fail.size() == 0) {
                    dbUtil.deleteUserGroup(userID);
                    String params = PARAMS + ",uid=" + mAuthId + ",operationType=logout";
                    long sid = mAuth.faceRecognize(params, new byte[]{0x01, 0x02}, faceDeleteListener);
                    if (sid == -1) {
                        Log.e(TAG, "注销人脸时参数错误");
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastShow.showTip(mToast, "退出组失败");
                        }
                    });
                }
            }
        };
        new Thread(delete).start();
    }

    /***
     * 裁剪图片
     *
     * @param activity
     *            Activity
     * @param uri
     *            图片的Uri
     */
    public void cropPicture(Activity activity, Uri uri) {
        Intent innerIntent = new Intent("com.android.camera.action.CROP");
        innerIntent.setDataAndType(uri, "image/*");
        innerIntent.putExtra("crop", "true");// 才能出剪辑的小方框，不然没有剪辑功能，只能选取图片
        innerIntent.putExtra("aspectX", 1); // 放大缩小比例的X
        innerIntent.putExtra("aspectY", 1);// 放大缩小比例的X 这里的比例为： 1:1
        innerIntent.putExtra("outputX", 320); // 这个是限制输出图片大小
        innerIntent.putExtra("outputY", 320);
        innerIntent.putExtra("return-data", false);
        // 切图大小不足输出，无黑框
        innerIntent.putExtra("scale", true);
        innerIntent.putExtra("scaleUpIfNeeded", true);
        innerIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getImagePath())));
        innerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        activity.startActivityForResult(innerIntent, REQUEST_INTENT_CROP);
    }

    public Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /**
     * 设置保存图片路径
     *
     * @return
     */
    private String getImagePath() {
        String path;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceDoor/";
        File folder = new File(path);
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        path += "crop.jpg";
        return path;
    }

    private String getImagePath2() {
        String path;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceDoor/";
        File folder = new File(path);
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        path += "flip.jpg";
        return path;
    }

    private void updateGallery(String filename) {
        MediaScannerConnection.scanFile(this, new String[]{filename}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }

    /**
     * 保存Bitmap至本地
     *
     * @param
     */
    private void saveBitmapToFile(Bitmap bmp) {
        String file_path = getImagePath();
        File file = new File(file_path);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree 旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotateImage(int angle, Bitmap bitmap) {
        // 图片旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 得到旋转后的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    private String userIdToAuthId(int userId) {
        return "a" + userId;
    }

    private int AuthIdToUserId(String authId) {
        return Integer.parseInt(authId.substring(1));
    }

    private IAuthListener faceRegListener = new IAuthListener() {

        @Override
        public void onVoiceprintResult(String arg0, int arg1, long arg2, String arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFusionResult(String arg0, int arg1, long arg2, String arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFaceResult(final String result, int arg1, long arg2, String arg3) {
            Log.e(TAG, "人脸注册结果：" + result);
            if (null != mProDialog) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressShow.stop(mProDialog, mLayout);
                    }
                });

            }
            try {
                JSONObject jsonResult = new JSONObject(result);
                String responseCode = jsonResult.getString("responseCode");
                // synchronize mAuthId and mGroupJoin and we only read it
                synchronized (RegisterActivity.this) {
                    if (responseCode.equals("0000")) {
                        DBUtil dbUtil = new DBUtil(RegisterActivity.this);
                        String staffName = ((EditText) findViewById(R.id.online_name)).getText().toString();
                        String staffID = ((EditText) findViewById(R.id.online_number)).getText().toString();
                        dbUtil.insertUser(staffID, staffName);
                        GroupManager groupManager = new GroupManager(RegisterActivity.this);
                        List<String> success = groupManager.joinGroup(mAuthId, mGroupJoin);
                        if (success.size() != mGroupJoin.size()) {
                            groupManager.quitGroup(mAuthId, success);
                        } else {
                            int userID = AuthIdToUserId(mAuthId);
                            dbUtil.insertUserGroup(userID, success);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastShow.showTip(mToast, "注册，加入组成功");
                                }
                            });
                            // 跳转到声纹识别界面
                            Intent intent = new Intent();
                            intent.putExtra("ID", mAuthId);
                            intent.setClass(RegisterActivity.this, VocalVerifyActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(RegisterActivity.this, "人脸注册错误：" + result,
                                        Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private IAuthListener faceDeleteListener = new IAuthListener() {

        @Override
        public void onVoiceprintResult(String arg0, int arg1, long arg2, String arg3) {

        }

        @Override
        public void onFusionResult(String arg0, int arg1, long arg2, String arg3) {

        }

        @Override
        public void onFaceResult(final String result, int arg1, long arg2, String arg3) {
            Log.e(TAG, "人脸注销结果：" + result);
            if (null != mProDialog) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressShow.stop(mProDialog, mLayout);
                    }
                });
            }

            try {
                JSONObject jsonResult = new JSONObject(result);
                String responseCode = jsonResult.getString("responseCode");
                if (responseCode.equals("0000")) {
                    int userID = AuthIdToUserId(mAuthId);
                    DBUtil dbUtil = new DBUtil(RegisterActivity.this);
                    dbUtil.deleteUser(userID);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastShow.showTip(mToast, "删除，退出组成功");
                        }
                    });
                    deleteVoicePrint(mAuthId);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(RegisterActivity.this, "人脸注销错误：" + result, Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void deleteVoicePrint(String mAuthId) {
            String REC_PARAMS_BASE = "uid=" + mAuthId + ","
                    + "appid=pc20onli,platform=Android,aue=speex-wb,featureType=voice,rgn=5,auf=audio/L16;rate=16000,sub=ivp,ssm=0,work_mode=digit_mode";
            String VP_RECOGNIZE_PARAMS_LOGOUT = REC_PARAMS_BASE + ",operationType=logout";
            mAuth.vpInit(VP_RECOGNIZE_PARAMS_LOGOUT, authListener);
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
                if ("logout".equals(sl)) {
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
                                ToastShow.showTip(mToast, "声纹删除成功");
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

    private Bitmap flipBitmap(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1);
        Bitmap flip = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        return flip;
    }
}
