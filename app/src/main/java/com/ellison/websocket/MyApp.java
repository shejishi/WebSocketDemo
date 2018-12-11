package com.ellison.websocket;

import android.app.Application;

/**
 * @author ellison
 * @date 2018年12月11日
 * @desc 用一句话描述这个类的作用
 */
public class MyApp extends Application {

    protected static MyApp application;


    static public MyApp getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        application = this;
    }
}
