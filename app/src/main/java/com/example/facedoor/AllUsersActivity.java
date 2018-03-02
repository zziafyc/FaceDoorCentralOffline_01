package com.example.facedoor;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.facedoor.adapter.BasicAdapter;
import com.example.facedoor.adapter.ViewHolder;
import com.example.facedoor.base.BaseAppCompatActivity;
import com.example.facedoor.db.DBUtil;
import com.example.facedoor.model.User;
import com.example.facedoor.util.PopUpWindowUtils;
import com.example.facedoor.util.ToastShow;

import java.util.ArrayList;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AllUsersActivity extends BaseAppCompatActivity {
    @Bind(R.id.lv_users)
    ListView mUsersLv;
    @Bind(R.id.img_more)
    ImageView moreImg;
    private Toast mToast;
    BasicAdapter<User> mAdapter;
    ArrayList<User> results = new ArrayList<>();

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_all_users;
    }

    @Override
    protected void initViewsAndEvents() {
       /* results.add(new User("123456","方应春"));
        results.add(new User("153267","秦齐"));*/
        initAdapter();
        initListener();
        initEvents();


    }

    private void initListener() {
        moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpWindowUtils.showPop(AllUsersActivity.this, moreImg);
            }
        });
    }

    private void initAdapter() {
        mAdapter = new BasicAdapter<User>(this, results, R.layout.item_user2) {
            @Override
            protected void render(ViewHolder holder, User item, int position) {

                holder.setText(R.id.item_userId, item.getUserID());
                holder.setText(R.id.item_userName, item.getName());
            }
        };
        mUsersLv.setAdapter(mAdapter);
    }

    private void initEvents() {
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        ArrayList<String> groupIds = MyApp.getDBManage(this).getGroupId();
        if (groupIds == null || groupIds.size() == 0) {
            ToastShow.showTip(mToast, "本机未建立组");
            return;
        }
        String groupID = groupIds.get(0);
        Log.e("fyc6", "组：" + groupID);
        System.out.println(groupID);
        Observable.just(groupID)
                .map(new Func1<String, ArrayList<String>[]>() {

                    @Override
                    public ArrayList<String>[] call(String arg0) {
                        DBUtil dbUtil = new DBUtil(AllUsersActivity.this);
                        ArrayList[] localUsers = null;
                        try {
                            localUsers = dbUtil.queryLocalUsers(arg0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return localUsers;
                    }

                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<String>[]>() {

                    @Override
                    public void call(ArrayList<String>[] arg0) {
                        if (arg0 == null)
                            return;
                        ArrayList<String> staffID = arg0[0];
                        ArrayList<String> name = arg0[1];
                        for (int i = 0; i < staffID.size(); i++) {
                            // results.add("工号：" + staffID.get(i) + "   " + "姓名：" + name.get(i));
                            results.add(new User(staffID.get(i), name.get(i)));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }
}
