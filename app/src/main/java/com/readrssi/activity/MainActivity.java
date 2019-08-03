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
 * ������
 * @author Administrator
 *
 */
public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity.class";
	private Context context = MainActivity.this;
	//������Ҫд����Ҫ�����豸����������
	private String scanBluetoothName="Autophix";
	// �������Ӱ�ť
		private Button btnConnect;
		//BLE RSSI   ���������豸��  ���������豸MAC  ��������״̬
		private TextView tvRssi,tvBluetoothName,tvbluetoothMac,tvConnectState;
		// ����������
		private BluetoothAdapter mBluetoothAdapter;
		
		private BluetoothOperate mBluetoothOperate;
		// ���� �ֻ����� ���ص�what
		private static final int REQUEST_ENABLE_BT = 1;
		// ���ܵ�RSSI what
		private static final int READRSSI=0;
		private static final int CONNECTED=1;
		private static final int DISCONNECT=2;
		// ��¼���������������ֺ͵�ַ
		private String bluetoothName, bluetoothMacAddress;
		// �Ƿ���ɨ�������豸״̬
		private boolean mScanning = false;
		private Handler mHandler;
		
		// Stops scanning after 10 seconds.
		// 10���ֹͣɨ��
		private static final long SCAN_PERIOD = 10000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// ȫ����ʾ
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
				// ���ݰ��ж�SDK��֧������BLE
				if (!getPackageManager().hasSystemFeature(
						PackageManager.FEATURE_BLUETOOTH_LE)) {
					// �������һ��handle ��˾�������棬�����û������Ƿ�֧��
					showToast("����ֻ�ϵͳ��֧������4.0");
				} else {
					// Initializes a Bluetooth adapter. For API level 18 and above,
					// get a reference to BluetoothAdapter through BluetoothManager.
					// ��ʼ������������ ����API18���ϣ�ͨ�� BluetoothManager���BluetoothAdapter
					final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
					mBluetoothAdapter = bluetoothManager.getAdapter();

				}
				// Checks if Bluetooth is supported on the device.
				// ��������Ӳ������Ƿ�֧�������豸
				if (mBluetoothAdapter == null) {
					// �������һ��handle ��˾�������棬�����û��ֻ�Ӳ���Ƿ�֧������
					showToast("����ֻ�����ģ�鲻֧������4.0");
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
		// ȷ�������豸�����á����û����������,��ʾһ���Ի���,Ҫ���û�����Ȩ��������
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
	 * ��ʾ��ʾ��˾
	 * 
	 * @param text
	 *            ��ʾ��˾���ַ���
	 */
	private void showToast(String text) {
		Toast.makeText(context, text, 1000).show();
	}

	/**
	 * ���������豸
	 *
	 * @param enable
	 *            �ɷ����������豸
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
		// �����豸ɨ�� �ص�
		// Ϊ�˷���BLE�豸��ʹ��startLeScan())���� ���������Ҫһ������BluetoothAdapter.LeScanCallback
		// �����ʵ�����Ļص��������Ǿ��Ƿ��ص�ɨ������
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
						// ����ҵ��Ƕ�Ӧ�������ֵĻ�
						if (device.getName() == null) return;
						Log.d(TAG, "device.getName()" + device.getName());
						if (device.getName().equals(scanBluetoothName)) {
							bluetoothName = device.getName();
							bluetoothMacAddress = device.getAddress();
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							mBluetoothOperate.setmDeviceAddress(bluetoothMacAddress);
							tvBluetoothName.setText(device.getName()+"");
							tvbluetoothMac.setText(bluetoothMacAddress+"");
							Log.d(TAG, "ɨ�赽�����豸 ��������="+bluetoothName+"MAC��ַ="+bluetoothMacAddress);
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
					tvConnectState.setText("��������");
					btnConnect.setText("CONNECTED");
					break;
				case DISCONNECT:
					tvConnectState.setText("���ӶϿ�");
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
					// ����BLE�豸
					scanLeDevice(true);
					//���ֻ��Ҫɨ�赽�ض����͵��豸����ʹ�ýӿ� ���ֻ��Ҫɨ�赽�ض����͵��豸��
					//��ʹ�ýӿ� startLeScan(UUID[], BluetoothAdapter.LeScanCallback)��ͨ��UUID�������豸��
					//startLeScan(UUID[], BluetoothAdapter.LeScanCallback)
					btnConnect.setText("Scanning");
				} else {
					//ֹͣɨ��BLE�豸
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
