package com.readrssi.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.readblerssi.R;
import com.readrssi.ble.BluetoothOperate;

import java.util.UUID;


public class MainActivity extends Activity implements OnClickListener {
    private static final String TAG = "MainActivity";
    private Context context = MainActivity.this;
    private String scanBluetoothName = "Autophix";
    UUID uuid = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    final UUID[] myUUID = {uuid};
    private Button btnConnect;
    private TextView tvRssi, tvName, tvMac, tvState;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothOperate mBluetoothOperate;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int READRSSI = 0;
    private static final int CONNECTED = 1;
    private static final int DISCONNECT = 2;
    private String bluetoothName, bluetoothMacAddress;
    private boolean mScanning = false;
    private Handler mHandler;
    // Stops scanning after 100 seconds.
    private static final long SCAN_PERIOD = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        btnConnect = (Button) findViewById(R.id.mainactivity_bluetooth_connect);
        btnConnect.setOnClickListener(this);
        tvRssi = (TextView) findViewById(R.id.rssi_value);
        tvName = (TextView) findViewById(R.id.bluetooth_name);
        tvMac = (TextView) findViewById(R.id.bluetooth_mac);
        tvState = (TextView) findViewById(R.id.bluetooth_connect_state);
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(getString(R.string.ble4_not_supported));
        } else {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                mBluetoothAdapter = bluetoothManager.getAdapter();
            }
        }
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            showToast(getString(R.string.ble4_not_supported));
        }
        mBluetoothOperate = BluetoothOperate.getInstance(this, mHandlerRssi);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != RESULT_OK) {
                    showToast(getString(R.string.bt_open_failed));
                }
                break;
            default:
                break;
        }
    }

    private void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }


    private void scanLeDevice(final boolean enable) {
        Log.d(TAG, "scanLeDevice");
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @SuppressWarnings("deprecation")
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                }
            }, SCAN_PERIOD);

            mScanning = true;
            //mBluetoothAdapter.startLeScan(mLeScanCallback);
            mBluetoothAdapter.startLeScan(myUUID, mLeScanCallback);

        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            Log.d(TAG, "onLeScan");
            Log.d(TAG, "rssi" + rssi);
            tvRssi.setText(rssi + "");
            tvName.setText(device.getName());
            tvMac.setText(device.getAddress());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device.getName() == null) return;
                    Log.d(TAG, "device.getName()" + device.getName());
                    if (device.getName().equals(scanBluetoothName)) {
                        bluetoothName = device.getName();
                        bluetoothMacAddress = device.getAddress();
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mBluetoothOperate.setmDeviceAddress(bluetoothMacAddress);
                        tvName.setText(device.getName());
                        tvMac.setText(bluetoothMacAddress);
                        Log.d(TAG, getString(R.string.ble_name) + bluetoothName + getString(R.string.ble_mac_addr) + bluetoothMacAddress);
                        mBluetoothOperate.OpenBluetoothService();

                    }
                }
            });
        }
    };
    @SuppressLint("HandlerLeak")
    Handler mHandlerRssi = new Handler() {

        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case READRSSI:
                    int rss = (int) msg.obj;
                    tvRssi.setText(rss);
                    break;
                case CONNECTED:
                    tvState.setText(R.string.ble_connected);
                    btnConnect.setText("CONNECTED");
                    break;
                case DISCONNECT:
                    tvState.setText(R.string.bt_disconnected);
                    btnConnect.setText("Scanning");
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainactivity_bluetooth_connect:
                if (!mScanning) {
                    scanLeDevice(true);
                    btnConnect.setText(R.string.scanning);
                } else {
                    scanLeDevice(false);
                    btnConnect.setText(R.string.start_scan);
                }
                break;

            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothOperate.CloseBluetoothService();
    }
}
