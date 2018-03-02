package com.example.facedoor.door;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.example.facedoor.MyApp;

import android.app.Activity;
import android.content.SharedPreferences;

public class DBPorxyComm extends AbstractComm{
	private static final int PORT = 7077;
//	private String dbIP;
//	private String doorNum;
	
	public DBPorxyComm(Activity activity){
		SharedPreferences config = activity.getSharedPreferences(MyApp.CONFIG, Activity.MODE_PRIVATE);
		ip = config.getString(MyApp.DB_AGENT, "");
		doorNum = config.getString(MyApp.DOOR_NUM, "");
		port = PORT;
	}
	
	public void sendNormalMessage(String staffID, File face){
		TCPClient tcpClient = new TCPClient();
		if(!tcpClient.Open(ip, port))
			return;
		
		byte[] headBuf;
		FileInputStream fileInputStream = null;
		try {
			headBuf = ("A" + staffID + doorNum).getBytes("utf-8");
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
	}
	
/*	public void sendAbnormalMessage(String type, File face){
		TCPClient tcpClient = new TCPClient();
		if(!tcpClient.Open(dbIP, PORT))
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
