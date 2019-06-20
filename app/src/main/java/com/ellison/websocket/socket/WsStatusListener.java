package com.ellison.websocket.socket;

import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

/**
 * @author ellison
 * @date 2019年06月20日
 * @desc 供外部调用监听器
 * <p>
 * 邮箱： Ellison.Sun0808@outlook.com
 * 博客： <a href="https://www.jianshu.com/u/b1c92a64018a">简书博客</a>
 */
public interface WsStatusListener {
    public void onOpen(WebSocket webSocket, Response response);

    public void onMessage(WebSocket webSocket, String text);

    public void onMessage(WebSocket webSocket, ByteString bytes);

    public void onClosing(WebSocket webSocket, int code, String reason);

    public void onClosed(WebSocket webSocket, int code, String reason);

    public void onFailure(WebSocket webSocket, Throwable t, @javax.annotation.Nullable Response response);
}
