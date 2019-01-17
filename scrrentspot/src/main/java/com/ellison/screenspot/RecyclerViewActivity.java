package com.ellison.screenspot;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * @author ellison
 * @date 2019年01月17日
 * @desc 用一句话描述这个类的作用
 */
public class RecyclerViewActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private Disposable mSubscribe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recycler_view);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        initData();

        subscribeClick(findViewById(R.id.btn), a -> {
            // 截图
            Observable
                    .defer((Callable<ObservableSource<Bitmap>>) () -> (ObservableSource<Bitmap>) observer -> observer.onNext(getRecyclerView(mRecyclerView)))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        File appCacheDir = null;
                        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(this)) {
                            String path = "/screen/" + ".temp";
                            appCacheDir = new File(Environment.getExternalStorageDirectory(), path);
                        }
                        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
                            appCacheDir = getFilesDir();
                            appCacheDir = new File(appCacheDir, ".temp");
                        }

                        File file = appCacheDir;
                        File takePhotoFile = new File(file, "share.png");
                        FileOutputStream fout = null;
                        try {
                            fout = new FileOutputStream(takePhotoFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);

                        Toast.makeText(this, "截图生成成功！", Toast.LENGTH_SHORT).show();
                    }, throwable -> {
                        Toast.makeText(this, "截图生成失败！", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSubscribe != null && !mSubscribe.isDisposed()) {
            mSubscribe.dispose();
        }
    }

    private void initData() {
        mSubscribe = Observable
                // defer函数是在子线程中运行
                .defer((Callable<ObservableSource<Response>>) () -> (ObservableSource<Response>) observer -> {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://v.juhe.cn/toutiao/index?type=&key=355a29a3019b9731eb6ba65dde107159")
                            .build();
                    client.newCall(request)
                            .enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    observer.onNext(response);
                                }
                            });
                })
                // 将Response数据获取到转换成String
                .map(response -> response.body().string())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                // 获取到json数据， 获取到List
                .subscribe(jsonStr -> {
                    RecyclerBean recyclerBean = JSON.parseObject(jsonStr, RecyclerBean.class);
                    RecyclerBean.ResultBean result = recyclerBean.getResult();
                    if (result != null) {
                        List<RecyclerBean.ResultBean.DataBean> data = result.getData();

                        if (data != null) {
                            mRecyclerView.setAdapter(new RecyclerViewAdapter(data));
                        }
                    }
                });
    }

    private Bitmap getRecyclerView(RecyclerView recyclerView) {
        //获取设置的adapter
        RecyclerViewAdapter adapter = (RecyclerViewAdapter) recyclerView.getAdapter();
        //创建保存截图的bitmap
        Bitmap bigBitmap = null;
        if (adapter != null) {
            //获取item的数量
            int size = adapter.getItemCount();
            //recycler的完整高度 用于创建bitmap时使用
            int height = 0;
            //获取最大可用内存
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // 使用1/8的缓存
            final int cacheSize = maxMemory / 8;
            //把每个item的绘图缓存存储在LruCache中
            LruCache<String, Bitmap> bitmapCache = new LruCache<>(cacheSize);

            for (int i = 0; i < size; i++) {
                //手动调用创建和绑定ViewHolder方法，
                RecyclerView.ViewHolder holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i));
                adapter.onBindViewSync((RecyclerViewAdapter.VH) holder, i);

                //测量
                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                //布局
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
                //开启绘图缓存
                Bitmap drawingCache = Bitmap.createBitmap(holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(drawingCache);
                canvas.drawColor(ContextCompat.getColor(this, android.R.color.white));
                holder.itemView.draw(canvas);

                if (drawingCache != null) {
                    bitmapCache.put(String.valueOf(i), drawingCache);
                }
                //获取itemView的实际高度并累加
                height += holder.itemView.getMeasuredHeight();
            }

            //根据计算出的recyclerView高度创建bitmap
            bigBitmap = Bitmap.createBitmap(recyclerView.getMeasuredWidth(), height, Bitmap.Config.RGB_565);
            //创建一个canvas画板
            Canvas canvas = new Canvas(bigBitmap);
            //当前bitmap的高度
            int top = 0;
            int left = 0;
            //画笔
            Paint paint = new Paint();
            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmapCache.get(String.valueOf(i));
                canvas.drawBitmap(bitmap, left, top, paint);

                left = 0;
                top += bitmap.getHeight();
            }
        }
        return bigBitmap;
    }


    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    public boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }
}
