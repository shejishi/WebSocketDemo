package com.ellison.screenspot;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * @author ellison
 * @date 2019年01月15日
 * @desc 普通的截图
 */
public class CommonSpotActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_spot);

        Button btn = findViewById(R.id.common_spot_btn);
        ImageView iv = findViewById(R.id.common_spot_copy_iv);

        subscribeClick(btn, action -> {
            // 截图
            View decorView = getWindow().getDecorView();
            decorView.buildDrawingCache();

            Bitmap drawingCache = decorView.getDrawingCache();
            iv.setImageBitmap(drawingCache);
        });
    }
}
