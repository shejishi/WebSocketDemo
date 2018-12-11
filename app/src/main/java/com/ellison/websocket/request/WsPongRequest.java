package com.ellison.websocket.request;

/**
 * @author ellison
 * @date 2018年11月26日
 * @desc 用一句话描述这个类的作用
 */
public class WsPongRequest extends WsRequest {


    /**
     * _method_ : pong
     */

    private String method;
    private String device;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

}
