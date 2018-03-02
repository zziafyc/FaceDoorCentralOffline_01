package com.example.facedoor.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.facedoor.MyApp;
import com.example.facedoor.R;
import com.example.facedoor.adapter.BasicAdapter;
import com.example.facedoor.adapter.ViewHolder;
import com.example.facedoor.model.Group;

import java.util.ArrayList;

public class DropEditText extends FrameLayout implements View.OnClickListener, OnItemClickListener {
    // 输入框
    private EditText mEditText;
    // 下拉按钮
    private ImageView mDropImage;
    //下拉标识
    private RelativeLayout mRv;
    // 弹出窗口
    private PopupWindow mPopup;
    // 弹出窗口的布局
    private WrapListView mPopView;

    private int mDrawableRight;
    // flower_parent or wrap_content
    private int mDropMode;
    private String mHit;
    BaseAdapter mAdapter;

    public DropEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.edit_layout, this);
        mPopView = (WrapListView) LayoutInflater.from(context).inflate(R.layout.pop_view, null);
        mPopView.setCacheColorHint(0);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DropEditText, defStyle, 0);
        mDrawableRight = ta.getResourceId(R.styleable.DropEditText_drawableRight, 0);
        mDropMode = ta.getInt(R.styleable.DropEditText_dropMode, 0);
        mHit = ta.getString(R.styleable.DropEditText_hint);
        ta.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mEditText = (EditText) findViewById(R.id.dropview_edit);
        mDropImage = (ImageView) findViewById(R.id.dropview_image);

        mEditText.setSelectAllOnFocus(true);
        mRv = (RelativeLayout) findViewById(R.id.img_rv);
        //mDropImage.setImageResource(mDrawableRight);

        //mEditText.setTextColor(android.graphics.Color.BLACK);
        //mEditText.setTextSize(20);

        if (!TextUtils.isEmpty(mHit)) {
            mEditText.setHint(mHit);
        }

        mDropImage.setOnClickListener(this);
        mPopView.setOnItemClickListener(this);
        mRv.setOnClickListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 如果布局发生改变，且dropMode为flower_parent
        // 则设置ListView的宽度为
        if (changed && 0 == mDropMode) {
            mPopView.setListWidth(getMeasuredWidth());
        }
    }

    /**
     * 设置ListView的Adapter
     *
     * @param adapter
     */
    public void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
        mPopView.setAdapter(adapter);

        mPopup = new PopupWindow(mPopView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopup.setFocusable(true); // 让popwin获取焦点
    }

    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    public void setList(final Context context, final ArrayList<Group> groups) {
        if (groups != null && groups.size() > 0) {
            mRv.setVisibility(View.VISIBLE);
        } else {
            mRv.setVisibility(View.GONE);
        }
        this.setAdapter(new BasicAdapter<Group>(context, groups, R.layout.item_group) {
            @Override
            protected void render(final ViewHolder holder, final Group item, final int position) {
                holder.setText(R.id.GroupName, item.getName());
                holder.setText(R.id.groupId, item.getId());
                holder.onClick(R.id.rl_group, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Group group = (Group) mPopView.getAdapter().getItem(position);
                        mEditText.setText(group.getId());
                        //设置光标始终靠右
                        if (mEditText.getText().toString().trim().length() > 0) {
                            CharSequence text = mEditText.getText();
                            if (text instanceof Spannable) {
                                Spannable spanText = (Spannable) text;
                                Selection.setSelection(spanText, text.length());
                            }
                        }
                        mPopup.dismiss();
                    }
                });
                holder.onClick(R.id.iv_delete, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        groups.remove(item);
                        mDropImage.setVisibility(GONE);
                        MyApp.getDBManage(context).deleteGroup(item.getId());
                        mPopup.dismiss();
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }


    private class MyAdapter extends BaseAdapter {
        private Context context;
        public ArrayList<String> arr;
        public ArrayList<String> arrShow;

        public MyAdapter(Context context, ArrayList<String> list, final ArrayList<String> showList) {
            super();
            this.context = context;
            arr = list;
            arrShow = showList;
        }

        @Override
        public int getCount() {
            if (arrShow != null && arrShow.size() != 0) {
                mDropImage.setVisibility(View.VISIBLE);
                return arrShow.size();
            } else {
                mDropImage.setVisibility(View.GONE);
                return 0;
            }

        }

        @Override
        public Object getItem(int position) {
            return arrShow.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(context);
            tv.setWidth(getMeasuredWidth());
            tv.setPadding(5, 10, 0, 10);
            tv.setTextColor(getResources().getColor(R.color.edit_text_color_bright_bg));
            tv.setTextSize(20);
            tv.setText(arr.get(position));
            return tv;
        }
    }

    /**
     * 获取输入框内的内容
     *
     * @return String content
     */
    public String getText() {
        return mEditText.getText().toString();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_rv) {
            if (mPopup.isShowing()) {
                mPopup.dismiss();
                return;
            }

            mPopup.showAsDropDown(this, 0, -5);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public EditText getEditText() {
        return mEditText;
    }

    public void setEditText(EditText editText) {
        mEditText = editText;
    }
}
