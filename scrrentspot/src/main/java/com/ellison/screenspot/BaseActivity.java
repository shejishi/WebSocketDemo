package com.ellison.screenspot;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author ellison
 * @date 2019年01月15日
 * @desc 用一句话描述这个类的作用
 */
public abstract class BaseActivity extends AppCompatActivity {

    private Disposable mClickDisposable;


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mClickDisposable != null && !mClickDisposable.isDisposed()) {
            mClickDisposable.dispose();
        }
    }

    protected void subscribeClick(View view, Consumer consumer) {
        mClickDisposable = RxView.clicks(view)
                .throttleFirst(1_000, TimeUnit.MILLISECONDS)
                .subscribe(consumer);
    }


    protected int getWidth() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    protected int getHeight() {
        return getResources().getDisplayMetrics().widthPixels;
    }

}
