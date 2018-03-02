package com.example.facedoor;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.example.facedoor.util.FaceRect;
import com.example.facedoor.util.FaceUtil;
import com.example.facedoor.util.ParseFDResult;
import com.example.facedoor.util.PopUpWindowUtils;
import com.iflytek.cloud.FaceDetector;
import com.iflytek.cloud.util.Accelerometer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class VideoDetect extends Activity implements DialogInterface.OnClickListener {
    private final static String TAG = VideoDetect.class.getSimpleName();
    private final static int FACE_WIDTH = 320;
    private final static int FACE_HEIGHT = 320;
    private static final String PASS_WORD = "123";
    private EditText editText;
    private ImageView moreImg;

    private SurfaceView mPreviewSurface;
    private SurfaceView mFaceSurface;
    private Camera mCamera;
    private int mCameraId = CameraInfo.CAMERA_FACING_FRONT;
    // Camera nv21格式预览帧的尺寸，默认设置640*480
    private int PREVIEW_WIDTH = 640;
    private int PREVIEW_HEIGHT = 480;
    // 预览帧数据存储数组和缓存数组
    private byte[] mNV21;
    private byte[] mBuffer;
    private byte[] mZero = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
    // 缩放矩阵
    private Matrix mScaleMatrix = new Matrix();
    // 加速度感应器，用于获取手机的朝向
    private Accelerometer mAcc;
    private FaceDetector mFaceDetector;
    private volatile boolean mStopTrack;
    private Toast mToast;
    private int isAlign = 0;
    private static int mFaceCount = 0;

    private Intent mIntent;
    private long lastTime;  //这个时间标记是最后人脸检测的时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detect);
        lastTime = System.currentTimeMillis();

        initUI();

        mNV21 = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
        mBuffer = new byte[PREVIEW_WIDTH * PREVIEW_HEIGHT * 2];
        mAcc = new Accelerometer(this);
        mFaceDetector = FaceDetector.createDetector(this, null);
        mIntent = new Intent(VideoDetect.this, IdentifyActivity.class);

        MyApp myApp = (MyApp) VideoDetect.this.getApplication();
        myApp.addActivity(this);
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
            editText = new EditText(VideoDetect.this);
            new AlertDialog.Builder(VideoDetect.this).setTitle("请输入密码").setIcon(R.drawable.ic_launcher)
                    .setView(editText).setPositiveButton("确定", VideoDetect.this).setNegativeButton("取消", null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Callback mPreviewCallback = new Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            closeCamera();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mScaleMatrix.setScale(width / (float) PREVIEW_WIDTH, height / (float) PREVIEW_HEIGHT);
        }
    };

    private void setSurfaceSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels;
        int height = (int) (width * PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);
        LayoutParams params = new LayoutParams(width, height);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        mPreviewSurface.setLayoutParams(params);
        mFaceSurface.setLayoutParams(params);
    }

    private void initUI() {
        moreImg = (ImageView) findViewById(R.id.img_more);
        mPreviewSurface = (SurfaceView) findViewById(R.id.sfv_preview);
        mFaceSurface = (SurfaceView) findViewById(R.id.sfv_face);

        mPreviewSurface.getHolder().addCallback(mPreviewCallback);
        // mPreviewSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mFaceSurface.setZOrderOnTop(true);
        mFaceSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // 点击SurfaceView，切换摄相头
        mFaceSurface.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 只有一个摄相头，不支持切换
                if (Camera.getNumberOfCameras() == 1) {
                    showTip("只有后置摄像头，不能切换");
                    return;
                }
                closeCamera();
                if (CameraInfo.CAMERA_FACING_FRONT == mCameraId) {
                    mCameraId = CameraInfo.CAMERA_FACING_BACK;
                } else {
                    mCameraId = CameraInfo.CAMERA_FACING_FRONT;
                }
                openCamera();
            }
        });

        setSurfaceSize();
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        moreImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpWindowUtils.showPop(VideoDetect.this, moreImg);
            }
        });
    }

    private void openCamera() {
        if (null != mCamera) {
            return;
        }

        if (!checkCameraPermission()) {
            showTip("摄像头权限未打开，请打开后再试");
            mStopTrack = true;
            return;
        }
        // 只有一个摄相头，打开后置
        if (Camera.getNumberOfCameras() == 1) {
            mCameraId = CameraInfo.CAMERA_FACING_BACK;
        }
        try {
            mCamera = Camera.open(mCameraId);
            // if (CameraInfo.CAMERA_FACING_FRONT == mCameraId) {
            // showTip("前置摄像头已开启，点击可切换");
            // } else {
            // showTip("后置摄像头已开启，点击可切换");
            // }
            showTip("摄像头已启动");
        } catch (Exception e) {
            e.printStackTrace();
            closeCamera();
            return;
        }
        Parameters params = mCamera.getParameters();
        params.setPreviewFormat(ImageFormat.NV21);
        params.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        mCamera.setParameters(params);
        // 设置显示的偏转角度，大部分机器是顺时针90度，某些机器需要按情况设置
        mCamera.setDisplayOrientation(0);
        mCamera.setPreviewCallback(new PreviewCallback() {

            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                System.arraycopy(data, 0, mNV21, 0, data.length);
            }
        });
        try {
            mCamera.setPreviewDisplay(mPreviewSurface.getHolder());
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean checkCameraPermission() {
        int status = checkPermission(permission.CAMERA, Process.myPid(), Process.myUid());
        if (PackageManager.PERMISSION_GRANTED == status) {
            return true;
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != mAcc) {
            mAcc.start();
        }

        mStopTrack = false;
        Runnable detect = new Runnable() {

            private int detectTimes = 0;
            private int noFacesCount = 0;
            SurfaceHolder surfaceHolder = null;

            @Override
            public void run() {
                surfaceHolder = mFaceSurface.getHolder();
                while (!mStopTrack) {
                    //规定检测时间为：
                    //规定检测时间为：
                    SharedPreferences config = getSharedPreferences(MyApp.CONFIG, MODE_PRIVATE);
                    String detectTimeValue = config.getString(MyApp.DETECT_TIME_VALUE, "60");
                    if (System.currentTimeMillis() - lastTime > Integer.parseInt(detectTimeValue) * 1000) {
                        finish();
                        break;
                    }

                    synchronized (mNV21) {
                        System.arraycopy(mNV21, 0, mBuffer, 0, mNV21.length);
                    }

                    // 获取手机朝向，返回值0,1,2,3分别表示0,90,180和270度
                    /*
                     * int direction = Accelerometer.getDirection(); boolean
					 * frontCamera = (Camera.CameraInfo.CAMERA_FACING_FRONT ==
					 * mCameraId); // 前置摄像头预览显示的是镜像，需要将手机朝向换算成摄相头视角下的朝向。 //
					 * 转换公式：a' = (360 - a)%360，a为人眼视角下的朝向（单位：角度） if
					 * (frontCamera) { // SDK中使用0,1,2,3,4分别表示0,90,180,270和360度
					 * direction = (4 - direction)%4; }
					 */
                    // 获取手机朝向，返回值0,1,2,3分别表示0,90,180和270度
                    int direction = Accelerometer.getDirection();
                    boolean frontCamera = (CameraInfo.CAMERA_FACING_FRONT == mCameraId);
                    // 前置摄像头预览显示的是镜像，需要将手机朝向换算成摄相头视角下的朝向。
                    // 转换公式：a' = (360 - a)%360，a为人眼视角下的朝向（单位：角度）
                    if (frontCamera) {
                        // SDK中使用0,1,2,3,4分别表示0,90,180,270和360度
                        direction = (4 - direction) % 4;
                    } else {
                        direction = 0;
                    }
                    ByteArrayOutputStream jpeg = nv21ToJPEG(mBuffer, PREVIEW_WIDTH, PREVIEW_HEIGHT);
                    byte[] rawJPEG = jpeg.toByteArray();
                    Bitmap cameraPic = BitmapFactory.decodeByteArray(rawJPEG, 0, rawJPEG.length);
                    saveBitmapToFile(cameraPic, "camera.jpg");

                    String result = mFaceDetector.trackNV21(mBuffer, PREVIEW_WIDTH, PREVIEW_HEIGHT, isAlign, direction);
                    Log.d(TAG, "result:" + result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        int ret = jsonObject.getInt("ret");
                        if (ret == 16005 || ret == 10106) {
                            mFaceDetector.destroy();
                            mFaceDetector = null;
                            mFaceDetector = FaceDetector.createDetector(VideoDetect.this, null);
                            // mStopTrack = true;
                            // MyApp myApp = (MyApp)
                            // VideoDetect.this.getApplication();
                            // myApp.onTerminate();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    FaceRect[] faces = ParseFDResult.parseResult(result);

                    if (faces == null || faces.length == 0) {
                        noFacesCount++;
                    }
                    //Log.e("VideoDetect", "faceCount:" + mFaceCount + "noFacesCount:" + noFacesCount);
                    if (mFaceCount > 0 && noFacesCount > 50) {
                        mFaceCount = 0;
//						mStopTrack = true;
//						MyApp myApp = (MyApp) VideoDetect.this.getApplication();
//						myApp.onTerminate();
                    }
                    // prevent multi faces
                    if (faces != null && faces.length != 1) {
                        continue;
                    }
                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                            canvas.setMatrix(mScaleMatrix);

                            if (faces == null) {
                                surfaceHolder.unlockCanvasAndPost(canvas);
                                continue;
                            }
                            if (null != faces && frontCamera == (CameraInfo.CAMERA_FACING_FRONT == mCameraId)) {

                                for (FaceRect face : faces) {
                                   /* face.bound = FaceUtil.RotateDeg90(face.bound, PREVIEW_WIDTH, PREVIEW_HEIGHT);
                                    face.bound = FaceUtil.RotateDeg90(face.bound, PREVIEW_HEIGHT, PREVIEW_WIDTH);
                                    face.bound = FaceUtil.RotateDeg90(face.bound, PREVIEW_WIDTH, PREVIEW_HEIGHT);*/

                                    if (face.point != null) {
                                        for (int i = 0; i < face.point.length; i++) {
                                            /*face.point[i] = FaceUtil.RotateDeg90(face.point[i], PREVIEW_WIDTH, PREVIEW_HEIGHT);
                                            face.point[i] = FaceUtil.RotateDeg90(face.point[i], PREVIEW_HEIGHT, PREVIEW_WIDTH);
                                            face.point[i] = FaceUtil.RotateDeg90(face.point[i], PREVIEW_WIDTH, PREVIEW_HEIGHT);*/

                                        }
                                    }

                                    FaceUtil.drawFaceRect(canvas, face, PREVIEW_WIDTH, PREVIEW_HEIGHT, frontCamera, false);
                                    //cameraPic = FaceUtil.rotateImage(90, cameraPic);
                                    saveBitmapToFile(cameraPic, "cameraRotated.jpg");
                                    cameraPic = cropWithFace(cameraPic, faces);
                                    saveBitmapToFile(cameraPic, "crop.jpg");

                                }
                            } else {
                                Log.d(TAG, "faces:0");
                            }

                            surfaceHolder.unlockCanvasAndPost(canvas);

                            if (faces.length > 0) {
                                lastTime = System.currentTimeMillis();
                                detectTimes++;
                                if (detectTimes == 20) {
                                    mFaceCount++;
                                    detectTimes = 0;
                                    mStopTrack = true;
                                    // too large, cause "FAILED Binder Transaction"
                                    // mIntent.putExtra("pic", cameraPic);
                                    // mHandler.sendEmptyMessage(START_IDENTIFY);
                                    startActivity(mIntent);
                                    int faceDetect = config.getInt(MyApp.FACEDETECT, 0);
                                    if (faceDetect == 0) {
                                        finish();
                                    }
                                }
                            }
                        }
                    }

                }
            }
        };
        new Thread(detect).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        if (null != mAcc) {
            mAcc.stop();
        }
        synchronized (mNV21) {
            System.arraycopy(mZero, 0, mNV21, 0, mNV21.length);
        }
        mStopTrack = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁对象
        mFaceDetector.destroy();
        // face++
        mFaceDetector = null;
        MyApp myApp = (MyApp) VideoDetect.this.getApplication();
        myApp.removeActivity(this);
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    private Bitmap cropWithFace(Bitmap org, FaceRect[] faces) {
        FaceRect face = faces[0];
        int left = face.bound.left;
        int top = face.bound.top;
        int right = face.bound.right;
        int bottom = face.bound.bottom;
        int faceX = right - left;
        int faceY = bottom - top;

        if (faceX >= FACE_WIDTH || faceY >= FACE_HEIGHT) {
            return org;
        }

        int paddingX = (FACE_WIDTH - faceX) / 2 + 1;
        int paddingY = (FACE_HEIGHT - faceY) / 2 + 1;
        left = left - paddingX;
        top = top - paddingY;

        left = left < 0 ? 0 : left;
        top = top < 0 ? 0 : top;
        int width = org.getWidth();
        int height = org.getHeight();
        int cropWidth = left + FACE_WIDTH > width ? width - left : FACE_WIDTH;
        int cropHeight = top + FACE_HEIGHT > height ? height - top : FACE_HEIGHT;

        return Bitmap.createBitmap(org, left, top, cropWidth, cropHeight);

    }

    private ByteArrayOutputStream nv21ToJPEG(byte[] nv21, int width, int height) {
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        Rect rect = new Rect(0, 0, yuv.getWidth(), yuv.getHeight());
        yuv.compressToJpeg(rect, 100, jpeg);
        return jpeg;
    }

    // private FaceRect[] convertFaceppToIFLY(Face[] faces, int width, int
    // height) {
    // if (faces == null) {
    // return null;
    // }
    // FaceRect[] iFlyFaces = new FaceRect[faces.length];
    // int i = 0;
    // for (Face face : faces) {
    // iFlyFaces[i] = new FaceRect();
    // iFlyFaces[i].bound.left = (int) (face.left * width);
    // iFlyFaces[i].bound.top = (int) (face.top * height);
    // iFlyFaces[i].bound.right = (int) (face.right * width);
    // iFlyFaces[i].bound.bottom = (int) (face.bottom * height);
    // i++;
    // }
    // return iFlyFaces;
    // }

    private Bitmap flipBitmap(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1);
        Bitmap flip = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        return flip;
    }

    /**
     * 设置保存图片路径
     *
     * @return
     */
    private String getImagePath(String fileName) {
        String path;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceVocal/";
        File folder = new File(path);
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        path += fileName;
        return path;
    }

    private void saveBitmapToFile(Bitmap bitmap, String fileName) {
        String file_path = getImagePath(fileName);
        File file = new File(file_path);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (editText.getText().toString().equals(PASS_WORD)) {
            finish();
        } else {
            Toast.makeText(VideoDetect.this, "密码错", Toast.LENGTH_SHORT).show();
        }
    }
}
