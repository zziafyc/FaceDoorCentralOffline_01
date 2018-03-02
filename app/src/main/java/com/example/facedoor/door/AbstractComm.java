package com.example.facedoor.door;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class AbstractComm {
	protected int port;
	protected String ip;
	protected String doorNum;

	public final void sendAbnormalMessage(String type, File face){
		TCPClient tcpClient = new TCPClient();
		if(!tcpClient.Open(ip, port))
			return;
		
		System.out.println(type + doorNum);
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
	}
}
