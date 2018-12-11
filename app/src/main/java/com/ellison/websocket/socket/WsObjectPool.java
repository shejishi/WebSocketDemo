package com.ellison.websocket.socket;


import com.ellison.websocket.request.WsPongRequest;

/**
 * @author ellison
 * @date 2018年11月26日
 * @desc 用一句话描述这个类的作用
 */
public class WsObjectPool {

    /**
     * 向服务器请求Pong
     */
    private static WsPongRequest PONG_INSTANCE;


    static {
        if (PONG_INSTANCE == null) {
            synchronized (WsObjectPool.class) {
                if (PONG_INSTANCE == null) {
                    PONG_INSTANCE = new WsPongRequest();
                    PONG_INSTANCE.setMethod(SocketConstants.EVENT_PONG);
                    PONG_INSTANCE.setDevice(SocketConstants.DEVICE_ANDROID);
                }
            }
        }
    }

    /**
     * 发送自检
     *
     * @return
     */
    public static WsPongRequest newPongRequest() {
        return PONG_INSTANCE;
    }
}
