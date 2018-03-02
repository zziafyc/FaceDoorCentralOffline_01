package com.example.facedoor;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.example.facedoor.base.BaseAppCompatActivity;
import com.example.facedoor.util.PopUpWindowUtils;

import butterknife.Bind;

public class QueryActivity2 extends BaseAppCompatActivity implements OnClickListener {
    @Bind(R.id.btn_query_groups)
    Button queryGroupsBtn;
    @Bind(R.id.btn_query_local_users)
    Button queryUsersBtn;
    @Bind(R.id.img_more)
    ImageView moreImg;

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_query2;
    }

    @Override
    protected void initViewsAndEvents() {
        queryGroupsBtn.setOnClickListener(this);
        queryUsersBtn.setOnClickListener(this);
        moreImg.setOnClickListener(this);

    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btn_query_groups:
                Intent intent1 = new Intent(this, AllGroupsActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_query_local_users:
                Intent intent2 = new Intent(this, AllUsersActivity.class);
                startActivity(intent2);
                break;
            case R.id.img_more:
                PopUpWindowUtils.showPop(QueryActivity2.this, moreImg);
                break;

            default:
                break;
        }
    }

}
