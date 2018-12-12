package com.ellison.websocket.socket;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ellison.websocket.request.WsRequest;
import com.ellison.websocket.response.LogoutResponse;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @author ellison
 * @date 2018年12月11日
 * @desc 用一句话描述这个类的作用
 */
@SuppressLint("CheckResult")
public class WebSocketService extends Service {
    /**
     * 连接WebSocket传的url
     */
    public static final String WEB_SOCKET_URL = "socket_url";
    private static final String LOG_TAG = "WebSocket";

    /**
     * 标记是否正在连接中，用于自检服务避免重复发起连接。
     */
    private boolean isAttemptConnecting;

    /**
     * 连续尝试连接次数，超过限制时触发手动诊断和上报过程。
     */
    private int connectionAttemptCount = 0;
    /**
     * 标识准备关闭Service。
     * 一旦这个标记为true,则onClose方法里不能再发起重连操作
     */
    private boolean preparedShutdown = false;
    /**
     * 标记是否需要自动重连。
     */
    private boolean shouldAutoReconnect;
    /**
     * 最高允许的连续失败次数。
     * 达到这个次数将立即触发上报操作。
     */
    private final int ATTEMPT_TOLERANCE = 2;

    /**
     * 为避免用户空闲太久导致WebSocket连接被服务器断开，需要定期向服务器发送Pong请求。
     * <p>Pong请求不需要服务器回复，相应地，服务器下发的Ping请求也不需要处理。</p>
     */
    private ScheduledExecutorService pongService;

    /**
     * 有时候会因为一些数据传输异常【如重复登录房间、或数据出现错误等】导致被服务器强行断开连接。
     * 为了避免这种情况下用户毫无察觉地不可用，在WebSocket初始化后创建一个定时自检的Service。
     * <p>
     * 为什么不能单纯依靠OnClose方法来完成重连？
     * 因为OnClose方法里的重连可能连接失败！失败后就再也没有OnClose了！
     */
    private Disposable mSelfCheckDispose;

    /**
     * 保存所有实现的接口页面
     */
    private HashMap<String, WsListener<?>> activeListener = new HashMap<>();

    private WebSocket mWebSocket;
    private String socketUrl;

    /**
     * 开始启动服务
     *
     * @param context
     * @return
     */
    public static Intent createIntent(Context context, String socketUrl) {
        Intent intent = new Intent(context, WebSocketService.class);
        intent.putExtra(WEB_SOCKET_URL, socketUrl);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "----- onBind -----");
        socketUrl = intent.getStringExtra(WEB_SOCKET_URL);
        return new ServiceBinder();
    }

    public class ServiceBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        socketUrl = intent.getStringExtra(WEB_SOCKET_URL);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "----- onCreate -----");
        initSocketWrapper("InitialConnect", true);
        startSelfCheckService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Use this to force restart service
        socketUrl = intent.getStringExtra(WEB_SOCKET_URL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "----- onDestroy -----");
    }

    /**
     * 只需给第一个参数原因
     *
     * @param forReason
     */
    private void initSocketWrapper(String forReason) {
        initSocketWrapper(forReason, false);
    }

    /**
     * 初始化
     *
     * @param startReason
     * @param isFirstConnect
     */
    private void initSocketWrapper(String startReason, boolean isFirstConnect) {
        Observable.just(startReason)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        if (isAttemptConnecting) {
                            Log.v(LOG_TAG, startReason + " : Should reconnect but already in process, skip.");
                            return Boolean.FALSE;
                        }
                        return Boolean.TRUE;
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        if ((mWebSocket == null) && (!isFirstConnect) && (!isAttemptConnecting)) {
                            showUiWebSocketStatus("与服务器失去连接！！！");
                        }
                    }
                })
                .observeOn(Schedulers.io())
                .subscribe(s -> initSocket());
    }


    /**
     * 初始化WebSocket
     */
    private void initSocket() {
        // 如果在连接，就不用连接了
        if (isAttemptConnecting) {
            return;
        }

        isAttemptConnecting = true;
        Log.v(LOG_TAG, "Set isAttemptConnecting flag to true");

        // 开始初始化
        Observable.create(new ObservableOnSubscribe<WebSocket>() {
            @Override
            public void subscribe(ObservableEmitter<WebSocket> emitter) throws Exception {
                connectionAttemptCount++;
                Log.d(LOG_TAG, "Connection attempt: " + connectionAttemptCount);

                //TODO 这里可以进行登录业务判断

                Request request = new Request.Builder()
                        .url(socketUrl)
                        .build();
                OkHttpClient client = new OkHttpClient();
                client.newWebSocket(request, new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        super.onOpen(webSocket, response);

                        isAttemptConnecting = false;
                        connectionAttemptCount = 0;

                        // 连接成功之后
                        mWebSocket = webSocket;

                        dispatchStringMessage("连接成功！！！");

                        emitter.onNext(mWebSocket);
                        emitter.onComplete();
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        super.onMessage(webSocket, text);

                        dispatchStringMessage(text);
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {
                        super.onMessage(webSocket, bytes);
                    }

                    @Override
                    public void onClosing(WebSocket webSocket, int code, String reason) {
                        super.onClosing(webSocket, code, reason);
                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        super.onClosed(webSocket, code, reason);

                        Log.i(LOG_TAG, "ClosedCallback: WebSocket closed.");

                        // 等待自检重启，或者自然关闭
                        if ((!preparedShutdown) && (shouldAutoReconnect)) {
                            initSocketWrapper("onClose");
                        }
                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, @javax.annotation.Nullable Response response) {
                        super.onFailure(webSocket, t, response);
                        dispatchStringMessage("连接失败！！！");

                        emitter.onError(t != null ? t : new ConnectException("Cannot connect we service!!!"));
                    }

                });
                client.dispatcher().executorService().shutdown();
            }
        })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebSocket>() {
                               @Override
                               public void accept(WebSocket webSocket) throws Exception {
                                   if (pongService == null) {
                                       startPongDaemonService();
                                   }
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d(LOG_TAG, "WebSocket init failed!");
                                throwable.printStackTrace();

                                // 判断是否需要执行诊断服务
                                if (connectionAttemptCount >= ATTEMPT_TOLERANCE) {
                                    Log.e(LOG_TAG, "Continuous connection error occurred for " + connectionAttemptCount + " times!");

                                    // 强制开始诊断服务
                                    Log.i(LOG_TAG, "Force starting diagnosis service");
                                    startService(new Intent(WebSocketService.this, NetworkDiagnosisService.class));

                                    // 重置标记
                                    connectionAttemptCount = 0;
                                }
                            }
                        });
    }

    /**
     * 对外提供销毁方法
     */
    public void prepareShutDown() {
        Log.i(LOG_TAG, "----- prepareShutdown -----");
        preparedShutdown = true;

        stopSelfCheckService();
        stopPongDaemonService();

        if (mWebSocket != null) {
            mWebSocket.close(1000, "");
            mWebSocket = null;
        }

        if (activeListener.size() > 0) {
            Log.e(LOG_TAG, "Force clear active listeners, count= " + activeListener.size());
            activeListener.clear();
        }
    }


    /**
     * 启动自检服务，按照周期执行
     */
    private void startSelfCheckService() {
        // 自检服务器打开
        mSelfCheckDispose = Observable
                .interval(10, 10, TimeUnit.SECONDS)
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        if (!shouldAutoReconnect) {
                            Log.i(LOG_TAG, "Auto reconnect has been disabled, maybe kicked?");
                        }
                        return shouldAutoReconnect;
                    }
                })
                .map(new Function<Long, Boolean>() {
                    @Override
                    public Boolean apply(Long aLong) throws Exception {
                        return checkSocketAvailable();
                    }
                })
                .subscribeOn(Schedulers.computation())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        Log.i(LOG_TAG, "Self check task has been scheduled per " + 10 + " seconds.");
                        shouldAutoReconnect = true;
                        Log.i(LOG_TAG, "Auto reconnect feature has been enabled.");
                    }
                })
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean webSocketAlive) throws Exception {
                                   if (webSocketAlive) {
                                       Log.v(LOG_TAG, "WebSocket self check: is alive.");
                                       return;
                                   }
                                   // 自检服务器打开
                                   initSocketWrapper("SelfCheckService");
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e(LOG_TAG, "Error while executing self check!" + throwable);
                            }
                        });

    }

    /**
     * 关闭自检服务
     */
    private void stopSelfCheckService() {
        if (mSelfCheckDispose != null && (!mSelfCheckDispose.isDisposed())) {
            mSelfCheckDispose.dispose();
            Log.i(LOG_TAG, "Self check service has been unSubscribed.");
        }
    }

    /**
     * 方法字符串
     *
     * @param message
     */
    private void dispatchStringMessage(String message) {
        Observable.just(message)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        WsListener<String> listener = (WsListener<String>) activeListener.get(SocketConstants.ResponseType.RESPONSE_STRING_MESSAGE);
                        Log.d(LOG_TAG, "Msg entity: " + s + ".");
                        if (listener != null) {
                            listener.handleData(s);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 检查WebSocket是否连接成功
     *
     * @return
     */
    private boolean checkSocketAvailable() {
        if (mWebSocket == null) {
            Log.e(LOG_TAG, "WebSocket not ready, ignore this operation!");
            return false;
        }
        return true;
    }

    /**
     * 消息分发
     *
     * @param jsonString
     */
    private void dispatchJsonMessage(String jsonString) {
        String type = null;
        try {
            JSONObject json = JSON.parseObject(jsonString);
            // 从json结构中获取到type类型数据
            type = json.getString("type");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Message is not well-formed data!");
        }
        // TODO 这里可以和后台商量 接受的json数据结构
        // {"type" : "login", "message":"", "data":"", "code":"1000" }

        if (TextUtils.isEmpty(type)) {
            Log.e(LOG_TAG, "Cannot parse type from msg!");
            return;
        }
        Log.v(LOG_TAG, "Dispatching msg type : " + type);

        switch (type) {
            // 登出操作
            case SocketConstants.ResponseType.EVENT_LOGIN:
                notifyListener(jsonString, type, LogoutResponse.class);
                break;
            default:
                break;
        }
    }

    /**
     * 将json数据转成Object
     *
     * @param msg
     * @param type
     * @param clazz
     * @param <T>
     */
    private <T> void notifyListener(String msg, String type, Class<T> clazz) {
        Log.d("《《type》》", "notifyListener: " + type);

        Observable.just(msg)
                .map(new Function<String, T>() {
                    @Override
                    public T apply(String s) throws Exception {
                        return JSON.parseObject(msg, clazz);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<T>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(T data) {
                        WsListener<T> listener = (WsListener<T>) activeListener.get(type);
                        if (listener == null) {
                            Log.e(LOG_TAG, "No listener handle type " + type + ", discard this.");
                            return;
                        }
                        Log.d(LOG_TAG, "Msg entity: " + data.toString() + ".");
                        listener.handleData(data);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, "Ws Service has catch an error! " + e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 给服务器发送Pong自检
     */
    private void startPongDaemonService() {
        pongService = Executors.newSingleThreadScheduledExecutor();
        pongService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (mWebSocket != null) {
                    sendRequest(WsObjectPool.newPongRequest());
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
        Log.i(LOG_TAG, "Pong service has been scheduled at " + 10 + " seconds delay.");
    }

    /**
     * 关闭对服务器Pong服务
     */
    private void stopPongDaemonService() {
        if (pongService != null && (!pongService.isShutdown())) {
            pongService.shutdownNow();
            Log.i(LOG_TAG, "Shutdown pong service now.");
        }
    }

    /**
     * 通过WebSocket发送消息  json数据
     *
     * @param request
     */
    public void sendRequest(WsRequest request) {
        // TODO 这里添加登录之后的token或者uid
//        request.setUid(userInfo.getUid());
//        request.setToken(userInfo.getToken());

        String msg = JSON.toJSONString(request);
        Log.d(LOG_TAG, "sending msg: " + msg);
        mWebSocket.send(msg);
    }

    /**
     * 在主线程中同志界面WebSocket状态
     *
     * @param msg
     */
    private void showUiWebSocketStatus(String msg) {
        Observable.just(msg)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show());
    }

    /**
     * Register listener for specified type.
     *
     * @param event    Event name. see {@link SocketConstants}
     * @param listener see {@link WsListener}
     */
    public void registerListener(@NonNull String event, @NonNull WsListener listener) {
        activeListener.put(event, listener);
    }

    /**
     * Remove all listeners.
     */
    public void removeAllListeners() {
        Log.i(LOG_TAG, "Removing all listeners, count= " + activeListener.size());
        activeListener.clear();
    }

}
