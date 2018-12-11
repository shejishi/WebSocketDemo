package com.ellison.websocket.response;

/**
 * @author ellison
 * @date 2018年12月11日
 * @desc 用一句话描述这个类的作用
 */
public class LogoutResponse {


    private String status;
    private String msg;
    String type;
    private String res;

    public String getStatus() {
        return status == null ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRes() {
        return res == null ? "" : res;
    }

    public void setRes(String res) {
        this.res = res;
    }
}
