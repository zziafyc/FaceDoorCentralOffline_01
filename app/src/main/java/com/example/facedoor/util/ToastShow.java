package com.example.facedoor.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastShow {
	private static Toast mToast;

	public static void showTip(final Toast toast, final String tip) {
		if (toast != null) {
			toast.setText(tip);
			toast.show();
		}
	}

	public static void showTip(final Toast toast, final int resId) {
		if (toast != null) {
			toast.setText(resId);
			toast.show();
		}
	}
	public static void showTip(Context context, String msg) {
		if (null != msg) {
			if (mToast == null) {
				mToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
				mToast.setGravity(Gravity.CENTER, 0, 0);
			} else {
				mToast.setText(msg);
			}
			mToast.show();
		}
	}
//	public static void showTipLong(){
//		
//	}
}
