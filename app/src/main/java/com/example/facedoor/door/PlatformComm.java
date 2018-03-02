package com.example.facedoor.door;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.example.facedoor.MyApp;

import android.app.Activity;
import android.content.SharedPreferences;

public class PlatformComm extends AbstractComm{
	private static final int PORT = 6666;
//	private String platformIP;
//	private String doorNum;
	
	public PlatformComm(Activity activity){
		SharedPreferences config = activity.getSharedPreferences(MyApp.CONFIG, Activity.MODE_PRIVATE);
		ip = config.getString(MyApp.PLATFORM_IP, "");
		doorNum = config.getString(MyApp.DOOR_NUM, "");
		port = PORT;
	}
	
/*	public void sendAbnormalMessage(String type, File face){
		TCPClient tcpClient = new TCPClient();
		if(!tcpClient.Open(platformIP, PORT))
			return;
		
		byte[] headBuf;
		FileInputStream fileInputStream = null;
		try {
			headBuf = (type + doorNum).getBytes("utf-8");
			tcpClient.writeBytes(headBuf);
			fileInputStream = new FileInputStream(face);
			int available = fileInputStream.available();
			byte[] faceBuf = new byte[available];
			fileInputStream.read(faceBuf);
			tcpClient.writeBytes(faceBuf);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(fileInputStream != null){
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			tcpClient.close();	
		}		
	}*/
}
