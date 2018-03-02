package com.example.facedoor.util;

import android.app.ProgressDialog;
import android.view.View;

public class ProgressShow {

	public static void show(ProgressDialog proDialog, String msg, View view){
		proDialog.setMessage(msg);
		proDialog.show();
		view.setEnabled(false);
	}
	
	public static void stop(ProgressDialog proDialog, View view){
		proDialog.dismiss();
		view.setEnabled(true);
	}
}
