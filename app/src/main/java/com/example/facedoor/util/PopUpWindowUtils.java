package com.example.facedoor.util;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.facedoor.R;

/**
 * Created by fyc on 2017/11/6.
 */

public class PopUpWindowUtils {

    public static void showPop(final Activity context, View parent) {
        final View contentView = LayoutInflater.from(context).inflate(R.layout.pop_exit, null, false);
        TextView textView = (TextView) contentView.findViewById(R.id.exit);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.finish();
            }
        });
        PopupWindow mPopupWindow = new PopupWindow(contentView, 80, 40, true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.showAsDropDown(parent, 20, 10);

    }
}
