package com.example.facedoor;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.facedoor.db.DBUtil;
import com.example.facedoor.util.PopUpWindowUtils;
import com.example.facedoor.util.ToastShow;

import java.util.ArrayList;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class QueryActivity extends Activity implements OnClickListener {
    ListView mListView;
    private Button button;
    private Toast mToast;
    private ImageView moreImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        mListView = (ListView) findViewById(R.id.lv_users);
        moreImg = (ImageView) findViewById(R.id.img_more);
        moreImg.setOnClickListener(this);

        findViewById(R.id.btn_query_users).setOnClickListener(this);
        button = (Button) findViewById(R.id.btn_query_local_users);
        button.setOnClickListener(this);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
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
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btn_query_users:
                Observable.create(new OnSubscribe<ArrayList<String>>() {
                    @Override
                    public void call(Subscriber<? super ArrayList<String>> arg0) {
                        DBUtil dbUtil = new DBUtil(QueryActivity.this);
                        ArrayList<String> name = new ArrayList<String>();
                        ArrayList<String> id = new ArrayList<String>();
                        ArrayList<String> results = new ArrayList<String>();
                        dbUtil.queryGroups(id, name);
                        for (int i = 0; i < name.size(); i++) {
                            results.add("组名：" + name.get(i) + "  " + "组ID：" + id.get(i));
                        }
                        arg0.onNext(results);
                    }
                }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ArrayList<String>>() {
                            @Override
                            public void call(ArrayList<String> arg0) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(QueryActivity.this,
                                        android.R.layout.simple_list_item_1, arg0);
                                mListView.setAdapter(adapter);
                                mListView.setBackgroundColor(getResources().getColor(R.color.listview));
                                v.setVisibility(View.INVISIBLE);
                            }
                        });
                break;
            case R.id.btn_query_local_users:
                // ArrayList<String> groupIds = MainActivity.db.getGroupId();
                ArrayList<String> groupIds = MyApp.getDBManage(this).getGroupId();
                if (groupIds.size() == 0) {
                    ToastShow.showTip(mToast, "本机未建立组");
                    return;
                }
                String groupID = groupIds.get(0);
                System.out.println(groupID);
                Observable.just(groupID).map(new Func1<String, ArrayList<String>[]>() {

                    @Override
                    public ArrayList<String>[] call(String arg0) {
                        DBUtil dbUtil = new DBUtil(QueryActivity.this);
                        ArrayList[] localUsers = null;
                        try {
                            localUsers = dbUtil.queryLocalUsers(arg0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return localUsers;
                    }

                }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ArrayList<String>[]>() {

                            @Override
                            public void call(ArrayList<String>[] arg0) {
                                if (arg0 == null)
                                    return;
                                ArrayList<String> staffID = arg0[0];
                                ArrayList<String> name = arg0[1];
                                ArrayList<String> results = new ArrayList<String>();
                                for (int i = 0; i < staffID.size(); i++) {
                                    results.add("工号：" + staffID.get(i) + "   " + "姓名：" + name.get(i));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(QueryActivity.this,
                                        android.R.layout.simple_list_item_1, results);
                                mListView.setAdapter(adapter);
                                mListView.setBackgroundColor(getResources().getColor(R.color.listview));
                                v.setVisibility(View.INVISIBLE);
                            }
                        });
                break;
            case R.id.img_more:
                PopUpWindowUtils.showPop(QueryActivity.this, moreImg);
                break;

            default:
                break;
        }
    }

}
