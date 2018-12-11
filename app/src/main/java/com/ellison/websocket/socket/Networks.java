package com.ellison.websocket.socket;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 该类提供网络相关常用辅助方法，包括判断网络是否已连接、打开网络设置等。
 * Created by Muyangmin on 15-8-31.
 * @author Muyangmin
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Networks {
    /**
     * Cannot instantiate.
     */
    private Networks(){
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * 判断网络是否已连接。
     *
     * @param context 上下文信息。
     * @return 除非网络已连接，一律返回false。
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return (info != null) && (info.isConnected());
        }
        return false;
    }

    /**
     * 判断当前连接的网络是否为Wi-Fi类型。
     * @param context 上下文信息。
     * @return 如果连接的是Wifi网络，则返回true。
     */
    public static boolean isWifiNetwork(Context context) {
        return getNetworkType(context) == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Return currently network type, if available. Otherwise it returns -1.
     */
    public static int getNetworkType(Context context){
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm!=null && cm.getActiveNetworkInfo()!=null){
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info.getType();
        }
        return -1;
    }

    /**
     * 打开网络连接设置界面。
     * @param activity 宿主Activity。
     */
    public static void openNetworkSetting(Activity activity){
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction(Intent.ACTION_VIEW);
        activity.startActivityForResult(intent, 0);
    }
}