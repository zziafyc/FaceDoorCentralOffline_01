package com.example.facedoor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.facedoor.adapter.BasicAdapter;
import com.example.facedoor.adapter.ViewHolder;
import com.example.facedoor.base.BaseAppCompatActivity;
import com.example.facedoor.db.DBUtil;
import com.example.facedoor.model.Group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by fyc on 2018/1/10.
 */

public class ChooseGroupActivity extends BaseAppCompatActivity {
    @Bind(R.id.title_tv_message)
    TextView titleTv;
    @Bind(R.id.img_more)
    ImageView moreIv;
    @Bind(R.id.title_right)
    TextView rightTv;
    @Bind(R.id.groupsLv)
    ListView mListView;
    @Bind(R.id.chooseIv)
    ImageView chooseIv;
    List<Group> mList = new ArrayList<>();
    List<Group> mChoosedList = new ArrayList<>();
    BasicAdapter<Group> mAdapter;
    boolean flag;

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_choose_group;
    }

    @Override
    protected void initViewsAndEvents() {
        initViews();
        initAdapter();
        initData();
        initListener();

    }

    private void initViews() {
        moreIv.setVisibility(View.GONE);
        titleTv.setText("选择相关组");
        rightTv.setVisibility(View.VISIBLE);
        rightTv.setText("完成");
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                List<Group> groups = (List<Group>) bundle.getSerializable("choosedGroups");
                if (groups != null && groups.size() > 0) {
                    mChoosedList.clear();
                    mChoosedList.addAll(groups);
                }
            }
        }
    }

    private void initAdapter() {
        mAdapter = new BasicAdapter<Group>(this, mList, R.layout.item_mygroup) {
            @Override
            protected void render(ViewHolder holder, final Group item, int position) {
                holder.setText(R.id.item_groupName, item.getName());
                holder.setText(R.id.item_groupId, item.getId());
                final CheckBox checkBox = holder.getSubView(R.id.checkbox);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            item.setChoose(true);
                            checkBox.setBackgroundResource(R.drawable.checked);
                        } else {
                            item.setChoose(false);
                            checkBox.setBackgroundResource(R.drawable.unchecked);
                        }
                    }
                });
                if (item.isChoose()) {
                    checkBox.setChecked(true);
                    checkBox.setBackgroundResource(R.drawable.checked);
                } else {
                    checkBox.setChecked(false);
                    checkBox.setBackgroundResource(R.drawable.unchecked);
                }
            }
        };
        mListView.setAdapter(mAdapter);

    }

    private void initData() {
        //从数据库获取数据
        Observable.create(new Observable.OnSubscribe<List<Group>>() {
            @Override
            public void call(Subscriber<? super List<Group>> subscriber) {
                DBUtil dbUtil = new DBUtil(ChooseGroupActivity.this);
                List<Group> groups = dbUtil.queryAllGroups();
                subscriber.onNext(groups);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Group>>() {
                    @Override
                    public void call(List<Group> groups) {
                        if (groups != null && groups.size() > 0) {
                            for (Group group : groups) {
                                if (mChoosedList != null && mChoosedList.size() > 0) {
                                    for (Group chooseGroup : mChoosedList) {
                                        if (group.getId().equals(chooseGroup.getId())) {
                                            group.setChoose(true);
                                            break;
                                        }
                                    }
                                }
                            }
                            mList.addAll(groups);
                            mAdapter.notifyDataSetChanged();
                        }
                        if (mChoosedList.size() == mList.size()) {
                            if (chooseIv != null) {
                                chooseIv.setBackgroundResource(R.drawable.deleteall3);
                                flag = true;
                            }
                        }
                    }
                });
    }

    private void initListener() {
        chooseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag) {
                    chooseIv.setBackgroundResource(R.drawable.deleteall3);
                    flag = true;
                    for (Group group : mList) {
                        group.setChoose(true);
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    chooseIv.setBackgroundResource(R.drawable.chooseall3);
                    flag = false;
                    for (Group group : mList) {
                        group.setChoose(false);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        rightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                List chooseGroups = new ArrayList();
                if (mList != null && mList.size() > 0) {
                    for (Group group : mList) {
                        if (group.isChoose()) {
                            chooseGroups.add(group);
                        }
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("choosedGroups", (Serializable) chooseGroups);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
