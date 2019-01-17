package com.ellison.screenspot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * @author ellison
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subscribeClick(findViewById(R.id.btn_common_spot), action -> {
            // 普通截图
            startActivity(new Intent(MainActivity.this, CommonSpotActivity.class));
        });
        subscribeClick(findViewById(R.id.btn_common_layout_spot), action -> {
            // 从layout截图
            startActivity(new Intent(MainActivity.this, LayoutScreenSpotActivity.class));
        });
        subscribeClick(findViewById(R.id.btn_common_spot_canvas), action -> {
            // 普通截图 canvas截图
            startActivity(new Intent(MainActivity.this, CommonCanvasSpotActivity.class));
        });
        subscribeClick(findViewById(R.id.btn_common_recycler_spot), action -> {
            // 普通RecyclerView截图
            startActivity(new Intent(MainActivity.this, RecyclerViewActivity.class));
        });
    }

}
