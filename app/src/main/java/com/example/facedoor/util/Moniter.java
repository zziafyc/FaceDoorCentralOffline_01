package com.example.facedoor.util;


import java.util.List;

import com.example.facedoor.MainActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;

public class Moniter extends Service{
	private static String PACKAGE = "com.example.facedoor";
	private static String ACTIVITY = ".MainActivity";
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();

		Notification.Builder builder = new Notification.Builder(Moniter.this);
		Intent intent = new Intent(this,MainActivity.class);
		builder.setContentTitle("moniter"); //设置标题
		builder.setContentText("moniter"); //消息内容
		PendingIntent pending = PendingIntent.getActivity(this, 0, intent, 0);
		builder.setContentIntent(pending);
		Notification notification = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			notification = builder.build();
			startForeground(1235, notification);
		}

		
		new Thread(check).start();
	}
	private Runnable check = new Runnable() {
		
		@Override
		public void run() {
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			boolean isRunning = false;
			while(true){
				isRunning = false;
				List<RunningAppProcessInfo> process = am.getRunningAppProcesses();
				for(RunningAppProcessInfo info : process){
					if (info.processName.equals(PACKAGE)) {
						isRunning = true;
						break;
					}
				}
				if (!isRunning) {
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ComponentName cn = new ComponentName(PACKAGE,PACKAGE + ACTIVITY);
					intent.setComponent(cn);
					startActivity(intent);
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
}
