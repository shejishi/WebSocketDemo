package com.ellison.screenspot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * @author ellison
 * @date 2019年01月17日
 * @desc 用一句话描述这个类的作用
 */
public class LayoutScreenSpotActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_scrren_spot);

        Button btn = findViewById(R.id.btn_layout);
        ImageView iv = findViewById(R.id.iv_layout);

        subscribeClick(btn, a -> {
            View view = LayoutInflater.from(LayoutScreenSpotActivity.this).inflate(R.layout.view_spreent_spot, null);
            view.measure(View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(getHeight(), View.MeasureSpec.EXACTLY));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            iv.setImageBitmap(bitmap);
        });
    }

}
