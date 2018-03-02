package com.example.facedoor.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;


public abstract class BaseAppCompatActivity extends AppCompatActivity {

    public static final int NON_CODE = -1;
    protected Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        // EventBus注册
        if (isBindEventBusHere()) {
            EventBus.getDefault().register(this);
        }
        // 加入到Activity栈
        BaseAppManager.getInstance().addActivity(this);
        //设置布局文件
        if (getContentViewLayoutID() != 0) {
            setContentView(getContentViewLayoutID());
        } else {
            throw new IllegalArgumentException("You must return a right contentView layout resource Id");
        }
        //相关事件都在这里处理
        initViewsAndEvents();
    }


    //设置返回键
    protected void onNavigateClick() {

    }

    //设置activity的布局
    protected abstract int getContentViewLayoutID();

    //onCreate核心事件处理在这里
    protected abstract void initViewsAndEvents();


    /**
     * 是否注册EventBus
     *
     * @return
     */
    protected boolean isBindEventBusHere() {
        return false;
    }

    /**
     * startActivity
     *
     * @param clazz target Activity
     */
    protected void go(Class<? extends Activity> clazz) {
        _goActivity(clazz, null, NON_CODE, false);
    }

    /**
     * startActivity with bundle
     *
     * @param clazz  target Activity
     * @param bundle
     */
    protected void go(Class<? extends Activity> clazz, Bundle bundle) {
        _goActivity(clazz, bundle, NON_CODE, false);
    }

    /**
     * startActivity then finish this
     *
     * @param clazz target Activity
     */
    protected void goAndFinish(Class<? extends Activity> clazz) {
        _goActivity(clazz, null, NON_CODE, true);
    }

    /**
     * startActivity with bundle and then finish this
     *
     * @param clazz  target Activity
     * @param bundle bundle extra
     */
    protected void goAndFinish(Class<? extends Activity> clazz, Bundle bundle) {
        _goActivity(clazz, bundle, NON_CODE, true);
    }

    /**
     * startActivityForResult
     *
     * @param clazz
     * @param requestCode
     */
    protected void goForResult(Class<? extends Activity> clazz, int requestCode) {
        _goActivity(clazz, null, requestCode, false);
    }

    /**
     * startActivityForResult with bundle
     *
     * @param clazz
     * @param bundle
     * @param requestCode
     */
    protected void goForResult(Class<? extends Activity> clazz, Bundle bundle, int requestCode) {
        _goActivity(clazz, bundle, requestCode, false);
    }

    /**
     * startActivityForResult then finish this
     *
     * @param clazz
     * @param requestCode
     */
    protected void goForResultAndFinish(Class<? extends Activity> clazz, int requestCode) {
        _goActivity(clazz, null, requestCode, true);
    }

    /**
     * startActivityForResult with bundle and then finish this
     *
     * @param clazz
     * @param bundle
     * @param requestCode
     */
    protected void goForResultAndFinish(Class<? extends Activity> clazz, Bundle bundle, int requestCode) {
        _goActivity(clazz, bundle, requestCode, true);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    public void finish() {
        super.finish();
        BaseAppManager.getInstance().removeActivity(this);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        if (isBindEventBusHere()) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    /**
     * Activity 跳转
     *
     * @param clazz  目标activity
     * @param bundle 传递参数
     * @param finish 是否结束当前activity
     */
    private void _goActivity(Class<? extends Activity> clazz, Bundle bundle, int requestCode, boolean finish) {
        if (null == clazz) {
            throw new IllegalArgumentException("you must pass a target activity where to go.");
        }
        Intent intent = new Intent(this, clazz);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        if (requestCode > NON_CODE) {
            startActivityForResult(intent, requestCode);
        } else {
            startActivity(intent);
        }
        if (finish) {
            finish();
        }
    }
}
