package com.ellison.websocket.socket;

/**
 * @author ellison
 * @date 2018年12月11日
 * @desc 用一句话描述这个类的作用
 */
public class SocketConstants {

    /**
     * 客户端上行心跳包，固定时间间隔重传。
     */
    public static final String EVENT_PONG = "pong";

    /**
     * 服务器下行心跳包，固定时间间隔重传。
     */
    public static final String EVENT_PING = "ping";

    /**
     * 发送Pong需要的字段
     */
    public static final String DEVICE_ANDROID = "android";

    /**
     * 后台
     */
    public static final String FIELD_TYPE = "type";

    public static class ResponseType {
        /**
         * 推送的是String
         */
        public static final String RESPONSE_STRING_MESSAGE = "string_message";

        /**
         * 被登出
         */
        public static final String EVENT_LOGIN = "logOut";
    }

}
