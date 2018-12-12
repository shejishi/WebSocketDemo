package com.ellison.websocket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @author ellison
 * @date 2018年12月11日
 * @desc 用一句话描述这个类的作用
 */
public class WebSocketService extends Service {

    public static final String LOG_TAG = "WebSocketTest";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "----- onBind -----");
        return new ServiceBinder();
    }

    public class ServiceBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "----- onCreate -----");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Use this to force restart service
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "----- onDestroy -----");
    }

}
