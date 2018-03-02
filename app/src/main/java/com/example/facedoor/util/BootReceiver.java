package com.example.facedoor.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.facedoor.FaceIndexActivity;

public class BootReceiver extends BroadcastReceiver{
	
	private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) {
			Toast.makeText(context, "boot completed", Toast.LENGTH_SHORT).show();
			Intent mainIntent = new Intent(context,FaceIndexActivity.class);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(mainIntent);
		}
	}
}
