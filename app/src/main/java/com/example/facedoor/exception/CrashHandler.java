package com.example.facedoor.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import com.example.facedoor.MyApp;

import android.content.Context;
import android.util.Log;

public class CrashHandler implements UncaughtExceptionHandler{
	 private static CrashHandler instance;  //单例引用，这里我们做成单例的，因为我们一个应用程序里面只需要一个UncaughtExceptionHandler实例
	 private MyApp myApp;   
	    private CrashHandler(){}
	    
	    public synchronized static CrashHandler getInstance(){  //同步方法，以免单例多线程环境下出现异常
	        if (instance == null){
	            instance = new CrashHandler();
	        }
	        return instance;
	    }
	    
	    public void init(Context ctx){  //初始化，把当前对象设置成UncaughtExceptionHandler处理器
	        Thread.setDefaultUncaughtExceptionHandler(this);
	        myApp = (MyApp)ctx;
	    }
	    
	    @Override
	    public void uncaughtException(Thread thread, Throwable ex) {  //当有未处理的异常发生时，就会来到这里。。 
	        Log.e("Sandy", "uncaughtException, thread: " + thread
	                + " name: " + thread.getName() + " id: " + thread.getId() + "exception: "
	                + ex);
	             String threadName = thread.getName();
	             if ("sub1".equals(threadName)){
	                   Log.d("Sandy", "xxx");
	             }else if(false){
	                      //这里我们可以根据thread name来进行区别对待，同时，我们还可以把异常信息写入文件，以供后来分析。
	             }
	             myApp.onTerminate();
	    }
}
