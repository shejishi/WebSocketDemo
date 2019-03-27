package com.ellison.screenspot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author ellison
 * @date 2019年01月09日
 * @desc 开启蓝牙测试界面
 */
public class BlueActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 100;
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * 显示当前扫描到的蓝牙设备
     */
    List<String> mArrayAdapter;
    private MyAdapter mMyAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blue);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_blue);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mArrayAdapter = new ArrayList<>();
        mMyAdapter = new MyAdapter();
        recyclerView.setAdapter(mMyAdapter);

        // 注册
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(v -> {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                mBluetoothAdapter.startDiscovery();

                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (bondedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : bondedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        mArrayAdapter.add(device.getName() + "\n"+  device.getAddress());
                        mMyAdapter.notifyDataSetChanged();
                    }
                }
            }

            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "当前设备不支持蓝牙设备！", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "打开蓝牙成功！", Toast.LENGTH_SHORT).show();

                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                // If there are paired devices
                if (bondedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : bondedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }
                mBluetoothAdapter.startDiscovery();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "打开蓝牙失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Create a BroadcastReceiver for ACTION_FOUND
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.d("发现设备：", device.getName() + "\n" + device.getAddress());
                mMyAdapter.notifyDataSetChanged();
            }
        }
    };

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_blue_tooth, null));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            myViewHolder.tvTitle.setText(mArrayAdapter.get(i));
        }

        @Override
        public int getItemCount() {
            return mArrayAdapter == null ? 0 : mArrayAdapter.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tvTitle;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.blue_tooth_tv);
            }
        }
    }

}
