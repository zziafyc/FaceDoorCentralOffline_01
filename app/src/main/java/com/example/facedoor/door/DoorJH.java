package com.example.facedoor.door;

import android.app.Activity;
import android.content.SharedPreferences;

import com.example.facedoor.MyApp;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DoorJH implements Openable {
    private String mIP;
    private int mPort;
    private String mDoorNum;
    private String mOpenTime;
    private String exceptionShow;

    private DoorJH(String ip, int port, String doorNum, String openTime) {
        mIP = ip;
        mPort = port;
        mDoorNum = doorNum;
        mOpenTime = openTime;
    }

    public DoorJH(Activity activity) {
        SharedPreferences config = activity.getSharedPreferences(MyApp.CONFIG, Activity.MODE_PRIVATE);
        mIP = config.getString(MyApp.DOORIP_KEY, "");
        mPort = 6001;
        mDoorNum = config.getString(MyApp.DOOR_CONTROLLER, "");
        mOpenTime = config.getString(MyApp.OPEN_TIME, "");
    }

    public void open() {
        TCPClient client = new TCPClient();
        Observable.just(client)
                .map(new Func1<TCPClient, String>() {
                    @Override
                    public String call(TCPClient client) {
                        if (!client.Open(mIP, mPort)) {
                            setExceptionShow(client.getExceptionShow());
                            return null;
                        }
                        client.writeString("RA-" + "0000" + mDoorNum + mOpenTime);
                        String response = client.readString();
                        client.close();
                        return response;
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

    public String getExceptionShow() {
        return exceptionShow;
    }

    public void setExceptionShow(String exceptionShow) {
        this.exceptionShow = exceptionShow;
    }
}
