package com.ellison.screenspot;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * @author ellison
 * @date 2019年01月15日
 * @desc 用一句话描述这个类的作用
 */
public class CommonCanvasSpotActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_spot);

        Button btn = findViewById(R.id.common_spot_btn);
        ImageView iv = findViewById(R.id.common_spot_copy_iv);

        subscribeClick(btn, action -> {
            // 使用canvas截图
            View decorView = getWindow().getDecorView();
            Bitmap bitmap = Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_4444);
            Canvas can = new Canvas(bitmap);
            decorView.draw(can);

            iv.setImageBitmap(bitmap);
        });

    }
}
