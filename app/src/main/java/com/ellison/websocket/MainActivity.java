package com.ellison.websocket;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ellison.websocket.request.WsRequest;
import com.ellison.websocket.request.WsStringRequest;
import com.ellison.websocket.socket.SocketConstants;
import com.ellison.websocket.socket.WebSocketService;
import com.ellison.websocket.socket.WsListener;
import com.ellison.websocket.utils.RxLifecycleUtils;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;

/**
 * @author ellison
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    /**
     * 来判断是否Service是否连接
     */
    private boolean isConnected = false;

    /**
     * WebSocket服务
     */
    @Nullable
    private WebSocketService mWebSocketService;
    private EditText mEt;
    private Button mBtnConnect;
    private Button mBtnDisConnect;
    private TextView mTvInfo;
    private EditText mEtData;
    private Button mBtnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEt = findViewById(R.id.et);
        mEt.setText("wss://echo.websocket.org");
        mBtnConnect = findViewById(R.id.btn_connect);
        mBtnDisConnect = findViewById(R.id.btn_dis_connect);
        mTvInfo = findViewById(R.id.tv_info);
        mTvInfo.setMovementMethod(ScrollingMovementMethod.getInstance());

        mEtData = findViewById(R.id.et_data);
        mBtnSend = findViewById(R.id.btn_send);

        RxView.clicks(mBtnConnect)
                .throttleFirst(1, TimeUnit.SECONDS)
                .as(RxLifecycleUtils.bindLifecycle(this))
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (TextUtils.isEmpty(mEt.getText().toString()) || isConnected) {
                            return;
                        }
                        // 连接结果
                        isConnected = bindService(WebSocketService.createIntent(MainActivity.this, mEt.getText().toString()), wsConnection, BIND_AUTO_CREATE);
                    }
                });

        RxView.clicks(mBtnDisConnect)
                .throttleFirst(1, TimeUnit.SECONDS)
                .as(RxLifecycleUtils.bindLifecycle(this))
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (mWebSocketService != null) {
                            mWebSocketService.prepareShutDown();
                            isConnected = false;
                        }
                        String s = mTvInfo.getText().toString();
                        if (!TextUtils.isEmpty(s)) {
                            mTvInfo.setText(s + "\n" + "关闭连接");
                        } else {
                            mTvInfo.setText("关闭连接");
                        }

                    }
                });

        RxView.clicks(mBtnSend)
                .throttleFirst(1, TimeUnit.SECONDS)
                .as(RxLifecycleUtils.bindLifecycle(this))
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (TextUtils.isEmpty(mEtData.getText().toString()) || !isConnected) {
                            return;
                        }
                        String s = mTvInfo.getText().toString();
                        if (!TextUtils.isEmpty(s)) {
                            mTvInfo.setText(s + "\n" + "客户端发送数据: " + mEtData.getText().toString());
                        } else {
                            mTvInfo.setText("客户端发送数据: " + mEtData.getText().toString());
                        }

                        // 发送消息
                        mWebSocketService.sendRequest(new WsStringRequest(mEtData.getText().toString()));
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unRegisterSocketAndBroadcast();
    }

    /**
     * 将Socket置空 取消Broadcast接受
     */
    private void unRegisterSocketAndBroadcast() {
        if (mWebSocketService != null) {
            mWebSocketService.prepareShutDown();
            if (isConnected) {
                unbindService(wsConnection);
                isConnected = false;
            }
        }
    }

    /**
     * Ws连接成功回调
     */
    private ServiceConnection wsConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service connected.");
            mWebSocketService = ((WebSocketService.ServiceBinder) service).getService();

            mWebSocketService.registerListener(SocketConstants.ResponseType.RESPONSE_STRING_MESSAGE, new WsListener() {
                @Override
                public void handleData(Object o) {
                    String s = mTvInfo.getText().toString();
                    if (!TextUtils.isEmpty(s)) {
                        mTvInfo.setText(s + "\n" + "接收服务器数据: " + o.toString());
                    } else {
                        mTvInfo.setText("接收服务器数据: " + o.toString());
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Service disconnected.");
            mWebSocketService = null;
        }
    };
}
