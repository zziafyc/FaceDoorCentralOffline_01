package com.example.facedoor.door;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TCPClient {
    private Socket mSocket;
    private OutputStream mOut;
    private InputStream mIn;
    private char[] mBuff = new char[100];
    private String exceptionShow;

    public boolean Open(String ip, int port) {
        setExceptionShow("");
        try {
            mSocket = new Socket(ip, port);
            mOut = mSocket.getOutputStream();
            mIn = mSocket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            setExceptionShow(e.getMessage());
            return false;
        }
        return true;
    }

    public void close() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                mSocket = null;
                mOut = null;
                mIn = null;
            }
        }
    }

    public void writeString(String str) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(mOut);
            writer.write(str);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readString() {
        String result = null;
        try {
            InputStreamReader reader = new InputStreamReader(mIn);
            int len = reader.read(mBuff);
            if (len == -1) {
                return null;
            }
            result = new String(mBuff, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void writeBytes(byte[] bytes) {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(mOut);
        try {
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getExceptionShow() {
        return exceptionShow;
    }

    public void setExceptionShow(String exceptionShow) {
        this.exceptionShow = exceptionShow;
    }
}
