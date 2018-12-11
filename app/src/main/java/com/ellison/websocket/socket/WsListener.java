package com.ellison.websocket.socket;

/**
 * @author ellison
 * @date 2018年11月26日
 * @desc 用一句话描述这个类的作用
 */
public interface WsListener<Data> {

    /**
     * 实现该接口来分发数据
     *
     * @param data
     */
    void handleData(Data data);
}
