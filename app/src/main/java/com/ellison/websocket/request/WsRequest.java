package com.ellison.websocket.request;

import java.io.Serializable;

/**
 * @author ellison
 * @date 2018年11月26日
 * @desc 所有的WebSocket发送消息都应该实现该接口
 */
public class WsRequest implements Serializable {
    private String token;
    private String uid;

    public String getToken() {
        return token == null ? "" : token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid == null ? "" : uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
