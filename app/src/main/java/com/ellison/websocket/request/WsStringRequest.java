package com.ellison.websocket.request;

import android.text.TextUtils;

/**
 * @author ellison
 * @date 2018年12月11日
 * @desc 用一句话描述这个类的作用
 */
public class WsStringRequest extends WsRequest {
    String message;

    public WsStringRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return TextUtils.isEmpty(message) ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
