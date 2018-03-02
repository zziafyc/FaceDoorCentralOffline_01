package com.example.facedoor;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.facedoor.adapter.BasicAdapter;
import com.example.facedoor.adapter.ViewHolder;
import com.example.facedoor.base.BaseAppCompatActivity;
import com.example.facedoor.db.DBUtil;
import com.example.facedoor.model.Group;
import com.example.facedoor.util.PopUpWindowUtils;

import java.util.ArrayList;

import butterknife.Bind;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AllGroupsActivity extends BaseAppCompatActivity {
    @Bind(R.id.lv_groups)
    ListView mGroupsLv;
    @Bind(R.id.img_more)
    ImageView moreImg;
    BasicAdapter<Group> mAdapter;
    ArrayList<Group> results = new ArrayList<>();

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_all_groups;
    }

    @Override
    protected void initViewsAndEvents() {
       /* results.add(new Group("12345","中用科技"));
        results.add(new Group("42345","科大讯飞"));*/
        initAdapter();
        initListener();
        initEvents();


    }

    private void initListener() {
        moreImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpWindowUtils.showPop(AllGroupsActivity.this, moreImg);
            }
        });
    }

    private void initAdapter() {
        mAdapter = new BasicAdapter<Group>(this, results, R.layout.item_group2) {
            @Override
            protected void render(ViewHolder holder, Group item, int position) {

                holder.setText(R.id.item_groupId, item.getId());
                holder.setText(R.id.item_groupName, item.getName());
            }
        };
        mGroupsLv.setAdapter(mAdapter);
    }

    private void initEvents() {
        Observable.create(new Observable.OnSubscribe<ArrayList<Group>>() {
            @Override
            public void call(Subscriber<? super ArrayList<Group>> arg0) {
                DBUtil dbUtil = new DBUtil(AllGroupsActivity.this);
                ArrayList<String> name = new ArrayList<>();
                ArrayList<String> id = new ArrayList<>();
                dbUtil.queryGroups(id, name);
                for (int i = 0; i < name.size(); i++) {
                    results.add(new Group(id.get(i), name.get(i)));
                }
                arg0.onNext(results);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<Group>>() {
                    @Override
                    public void call(ArrayList<Group> arg0) {
                        mAdapter.notifyDataSetChanged();
                    }
                });


    }
}
