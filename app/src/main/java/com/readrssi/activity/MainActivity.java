package com.readrssi.activity;



import com.example.readblerssi.R;
import com.readrssi.ble.BluetoothOperate;

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
/**
 * 主界面
 * @author Administrator
 *
 */
public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity.class";
	private Context context = MainActivity.this;
	//这里需要写入需要链接设备的蓝牙名字
	private String scanBluetoothName="Autophix";
	// 蓝牙链接按钮
		private Button btnConnect;
		//BLE RSSI   链接蓝牙设备名  链接蓝牙设备MAC  蓝牙连接状态
		private TextView tvRssi,tvBluetoothName,tvbluetoothMac,tvConnectState;
		// 蓝牙适配器
		private BluetoothAdapter mBluetoothAdapter;
		
		private BluetoothOperate mBluetoothOperate;
		// 开启 手机蓝牙 返回的what
		private static final int REQUEST_ENABLE_BT = 1;
		// 接受到RSSI what
		private static final int READRSSI=0;
		private static final int CONNECTED=1;
		private static final int DISCONNECT=2;
		// 记录搜索到的蓝牙名字和地址
		private String bluetoothName, bluetoothMacAddress;
		// 是否处于扫描蓝牙设备状态
		private boolean mScanning = false;
		private Handler mHandler;
		
		// Stops scanning after 10 seconds.
		// 10秒后停止扫描
		private static final long SCAN_PERIOD = 10000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 全屏显示
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				setContentView(R.layout.activity_main);
				btnConnect=(Button) findViewById(R.id.mainactivity_bluetooth_connect);
				btnConnect.setOnClickListener(this);
				tvRssi=(TextView) findViewById(R.id.rssi_value);
				tvBluetoothName=(TextView) findViewById(R.id.bluetooth_name);
				tvbluetoothMac=(TextView) findViewById(R.id.bluetooth_mac);
				tvConnectState=(TextView) findViewById(R.id.bluetooth_connect_state);
				mHandler = new Handler();
				// 根据包判断SDK否支持蓝牙BLE
				if (!getPackageManager().hasSystemFeature(
						PackageManager.FEATURE_BLUETOOTH_LE)) {
					// 这里添加一个handle 土司给主界面，告诉用户蓝牙是否支持
					showToast("你的手机系统不支持蓝牙4.0");
				} else {
					// Initializes a Bluetooth adapter. For API level 18 and above,
					// get a reference to BluetoothAdapter through BluetoothManager.
					// 初始化蓝牙适配器 ，在API18以上，通过 BluetoothManager获得BluetoothAdapter
					final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
					mBluetoothAdapter = bluetoothManager.getAdapter();

				}
				// Checks if Bluetooth is supported on the device.
				// 根据蓝牙硬件检查是否支持蓝牙设备
				if (mBluetoothAdapter == null) {
					// 这里添加一个handle 土司给主界面，告诉用户手机硬件是否支持蓝牙
					showToast("你的手机蓝牙模块不支持蓝牙4.0");
				}
			mBluetoothOperate=BluetoothOperate.getInstance(this,mHandlerRssi);
			
			}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		// 确保蓝牙设备上启用。如果没有启用蓝牙,显示一个对话框,要求用户授予权限启用它
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == RESULT_OK) {

			} else {

			}

			break;

		default:
			break;
		}
	}
	/**
	 * 显示提示土司
	 * 
	 * @param text
	 *            显示土司的字符串
	 */
	private void showToast(String text) {
		Toast.makeText(context, text, 1000).show();
	}

	/**
	 * 搜索蓝牙设备
	 *
	 * @param enable
	 *            可否搜索蓝牙设备
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
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
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		
	}
	// Device scan callback.
		// 蓝牙设备扫描 回调
		// 为了发现BLE设备，使用startLeScan())方法 这个方法需要一个参数BluetoothAdapter.LeScanCallback
		// 你必须实现它的回调函数，那就是返回的扫描结果。
		@SuppressLint("NewApi")
		private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

			@Override
			public void onLeScan(final BluetoothDevice device, int rssi,
					byte[] scanRecord) {
				Log.d(TAG, "onLeScan");
				Log.d(TAG, "device.getAddress()" + device.getAddress());
				Log.d(TAG, "rssi" + rssi);

				if (device.getName() == null) return;
				Log.d(TAG, "device.getName()" + device.getName());

				runOnUiThread(new Runnable() {
					@SuppressWarnings("deprecation")
					@SuppressLint({ "NewApi", "HandlerLeak" })
					@Override
					public void run() {
						// 如果找到是对应蓝牙名字的话
						if (device.getName() == null) return;
						Log.d(TAG, "device.getName()" + device.getName());
						if (device.getName().equals(scanBluetoothName)) {
							bluetoothName = device.getName();
							bluetoothMacAddress = device.getAddress();
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							mBluetoothOperate.setmDeviceAddress(bluetoothMacAddress);
							tvBluetoothName.setText(device.getName()+"");
							tvbluetoothMac.setText(bluetoothMacAddress+"");
							Log.d(TAG, "扫描到蓝牙设备 蓝牙名字="+bluetoothName+"MAC地址="+bluetoothMacAddress);
							mBluetoothOperate.OpenBluetoothService();
							
						}
					}
				});
			}
		};
		@SuppressLint("HandlerLeak")
		Handler mHandlerRssi=new Handler(){

			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case READRSSI:
					int rss=(int)msg.obj;
					tvRssi.setText(rss+"");
					break;
				case CONNECTED:
					tvConnectState.setText("已链接上");
					btnConnect.setText("CONNECTED");
					break;
				case DISCONNECT:
					tvConnectState.setText("链接断开");
					btnConnect.setText("Scanning");
					break;
				default:
					break;
				}
			}
			
		};
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.mainactivity_bluetooth_connect:
				if (!mScanning) {
					// 搜索BLE设备
					scanLeDevice(true);
					//如果只是要扫描到特定类型的设备，则使用接口 如果只是要扫描到特定类型的设备，
					//则使用接口 startLeScan(UUID[], BluetoothAdapter.LeScanCallback)，通过UUID来查找设备。
					//startLeScan(UUID[], BluetoothAdapter.LeScanCallback)
					btnConnect.setText("Scanning");
				} else {
					//停止扫描BLE设备
					scanLeDevice(false);
					btnConnect.setText("connect");
				}
				break;

			default:
				break;
			}
			
		}
		@Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			mBluetoothOperate.CloseBluetoothService();
		}
}
