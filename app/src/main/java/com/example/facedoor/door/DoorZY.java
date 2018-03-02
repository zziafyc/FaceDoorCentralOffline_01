package com.example.facedoor.door;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DoorZY implements Openable {
	private String mIP;
	private int mPort;
	
	public DoorZY(String ip, int port){
		mIP = ip;
		mPort = port;
	}

	@Override
	public void open() {
		DatagramSocket socket = null;
		InetAddress addr = null;
		
		try {
			addr = InetAddress.getByName(mIP);
			socket = new DatagramSocket();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(SocketException e){
			e.printStackTrace();
		}
		
		if(socket == null || addr == null){
			return;
		}
		
		final DatagramSocket finalSocket = socket;
		final InetAddress finalAddr = addr;
		byte[] sendBytes = "1510".getBytes();
		final DatagramPacket packet = new DatagramPacket(sendBytes, sendBytes.length, addr, mPort);
		
		Observable.create(new OnSubscribe<String>(){
			@Override
			public void call(Subscriber<? super String> arg0) {
				try {
					finalSocket.send(packet);
					byte[] endBytes = "0000".getBytes();
					packet.setData(endBytes);
					finalSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					finalSocket.close();
				}
			}
		})
		.subscribeOn(Schedulers.newThread())
		.subscribe(new Action1<String>() {
			@Override
			public void call(String arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public String getExceptionShow() {
		return null;
	}

	@Override
	public void setExceptionShow(String exceptionShow) {

	}
}
