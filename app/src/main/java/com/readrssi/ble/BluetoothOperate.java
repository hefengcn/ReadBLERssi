package com.readrssi.ble;




import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * ����BLE BluetoothLeService �ԽӲ�����
 * 
 * @author Administrator
 *
 */
@SuppressLint("NewApi")
public class BluetoothOperate {

	// ����ģʽ
	private static BluetoothOperate instance = new BluetoothOperate();

	private BluetoothOperate() {
	}

	public static BluetoothOperate getInstance() {
		return instance;
	}

	public static BluetoothOperate getInstance(Context context1,Handler handler) {
		context = context1;
		mHandler=handler;
		return instance;
	}
	
	private static final String TAG = "BluetoothOperat";
	// ����BLE����֪ͨ����
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	// ����BLE ������
	private BluetoothGattCharacteristic readCharacteristic;
	// ����BLE д����
	private BluetoothGattCharacteristic writeCharacteristic;
	// BLE������������
	private BluetoothLeService mBluetoothLeService = null;
	private BluetoothGattCharacteristic mGattCharacteristics;
	private static Context context;
	// ���ݸ�״̬�ж� ��������
	private boolean mConnected = false;
	//Ҫ���������豸��ַ
	private static String mDeviceAddress;
	//��activity ��������handler
	private static Handler mHandler;
	// ���ܵ�RSSI what
			private static final int READRSSI=0;
			//�����������豸 what
			private static final int CONNECTED=1;
			//�������ӶϿ� what
			private static final int DISCONNECT=2;

			private boolean isReadRssi=false;
	public void setmDeviceAddress(String mDeviceAddress) {
		BluetoothOperate.mDeviceAddress = mDeviceAddress;
	}

	/**
	 * ��ȡ����������
	 * 
	 * @return
	 */
	public BluetoothLeService getmService() {
		return mBluetoothLeService;
	}

	/**
	 * ��������������
	 * 
	 * @param mService
	 */
	public void setmService(BluetoothLeService mService) {
		this.mBluetoothLeService = mService;
	}

	/**
	 * ������������
	 */
	public void OpenBluetoothService() {
		
		// �ж����������Ƿ��
		if (mBluetoothLeService == null) {
			// �����󶨷�����ͼ
			Intent gattServiceIntent = new Intent(context,
					BluetoothLeService.class);
			// ����������
			context.bindService(gattServiceIntent, mServiceConnection,
					1);

			// ע��㲥 ���󶨶��action
			context.registerReceiver(mGattUpdateReceiver,
					makeGattUpdateIntentFilter());
			if (mBluetoothLeService != null) {
				// ���ݵ�ַ���� ���鿴�Ƿ��������
				final boolean result = mBluetoothLeService
						.connect(mDeviceAddress);
				Log.d(TAG, "Connect request result�����Ƿ�������=" + result);
			}

		}

	}

	/**
	 * �ر���������
	 */
	public void CloseBluetoothService() {
		// TODO Auto-generated method stub
		isReadRssi=false;
		if(readRSSI.isAlive()){
			readRSSI.interrupt();
		}
		// ���㲥
		mBluetoothLeService.disconnect();
		context.unregisterReceiver(mGattUpdateReceiver);
		// �ر���������
		if (mBluetoothLeService != null) {
			// �����������
			context.unbindService(mServiceConnection);
			// ��ҹ�����ͷ�
			mBluetoothLeService = null;
			readCharacteristic = null;
			writeCharacteristic = null;
			Log.d(TAG, "ͣ��������������");
			
			
		}
	}

	// Code to manage Service lifecycle.
	// ���� ������� ��������
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			Log.d(TAG, "����ʼ��");
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			// ��ʼ���������񣬲��ж��Ƿ���ʵ����
			if (!mBluetoothLeService.initialize()) {
				// ���ܳ�ʼ������
				Log.e(TAG, "Unable to initialize Bluetooth ���ܳ�ʼ������");

			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			// �Զ����ӵ�װ���ϳɹ�������ʼ��
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
			Log.d(TAG, "ͣ����������");
		}
	};

	

	/**
	 * IntentFilter��������˵�����޷���Ӧ�ʹ����Intent��ֻ���Լ����ĵ�Intent���ս������д���
	 * IntentFilterʵ�С���������������ֻ�г����������ܵ�Intent����IntentFilterֻ�������ʽIntent��
	 * ��ʽ��Intent��ֱ�Ӵ��͵�Ŀ������� Android���������һ������IntentFilter��ÿ��IntentFilter֮���໥����
	 * ��ֻ��Ҫ����һ����֤ͨ����ɡ��������ڹ��˹㲥��IntentFilter�����ڴ����д����⣬
	 * ������IntentFilter������AndroidManifest.xml�ļ��н���������
	 * 
	 * @return
	 */
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_WRITE_DATA);
		intentFilter.addAction(BluetoothLeService.ACTION_READ_ALL_DATA);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		return intentFilter;
	}

	

	
	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ������GATT����
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// �ϵ�����GATT����
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ����GATT����
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	// ���豸���յ����ݡ�����һ������֪ͨ�����Ľ��

	// ͨ��������Ʋ�ͬ���¼�
	// ACTION_GATT_CONNECTED: ���ӵ�GATT��������
	// ACTION_GATT_DISCONNECTED: ��GATT�������Ͽ����ӡ�
	// ACTION_GATT_SERVICES_DISCOVERED: ����GATT����
	// ACTION_DATA_AVAILABLE: ���豸���յ������ݡ�������Ƕ���֪ͨ�����Ľ����
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		private int time = 0, lost = 0, OK = 0;

		@Override
		public void onReceive(Context context, Intent intent) {

			final String action = intent.getAction();
			Log.d(TAG, "mGattUpdateReceiver �յ�action " + action);
			// ACTION_GATT_CONNECTED: ���ӵ�GATT��������
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				// ����״̬ ����
				mConnected = true;
				Log.d(TAG, "mGattUpdateReceiver ���ӵ�GATT�������� " );
				isReadRssi=true;
				mHandler.sendEmptyMessage(CONNECTED);
				readRSSI.start();
				// ACTION_GATT_DISCONNECTED: ��GATT�������Ͽ����ӡ�
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				// ����״̬ �Ͽ�
				mConnected = false;
				isReadRssi=false;
				mHandler.sendEmptyMessage(DISCONNECT);
				Log.d(TAG, "mGattUpdateReceiver ��GATT�������Ͽ����ӡ� " );
				// ACTION_GATT_SERVICES_DISCOVERED: ����GATT����
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				//�����������л�ȡд���� �ڷ������ݵ�ʱ����ҪЩ����
				writeCharacteristic=mBluetoothLeService.getWriteCharacteristic();
				Log.i(TAG, "writeCharacteristic�Ƿ����null "+(writeCharacteristic==null));
				Log.d(TAG, "mGattUpdateReceiver ����GATT���� " );
				// ACTION_DATA_AVAILABLE: ���豸���յ������ݡ�������Ƕ���֪ͨ�����Ľ����
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				String str = intent
						.getStringExtra(BluetoothLeService.ACTION_READ_DATA);
				Log.d(TAG, "mGattUpdateReceiver ���豸���յ������ݡ�������Ƕ���֪ͨ�����Ľ����" );
				
			} else if (BluetoothLeService.ACTION_WRITE_DATA.equals(action)) {
				Log.d(TAG, "mGattUpdateReceiver �յ�д�Ĺ㲥�˵�");
				String str = intent
						.getStringExtra(BluetoothLeService.ACTION_WRITE_DATA);

				Log.d(TAG, str);
			} else if (BluetoothLeService.ACTION_READ_ALL_DATA.equals(action)) {
				byte [] read=intent.getByteArrayExtra(BluetoothLeService.ACTION_READ_ALL_DATA);
				StringBuffer str = new StringBuffer();
				for (byte item : read) {
					str.append(String.format("%02X ", item));
				}
				Log.d(TAG, "mGattUpdateReceiver���յ����������� ����"+str);
				//���豸��ʼɨ��ʱ��
			}
		}
	};
	
	/**
	 * ��ȡ����RSSi�߳�
	 */
	Thread readRSSI =new Thread(){
		int Rssi=0;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (isReadRssi){
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//�����ȡ����RSSi�ص��ɹ�
				if(mBluetoothLeService.getRssiVal()){
					//��ȡ�Ѿ�������RSSIֵ
					Rssi=BluetoothLeService.getBLERSSI();
					mHandler.obtainMessage(READRSSI, Rssi).sendToTarget();
				}
				
			}
			
		}
		
	};
	//����Ҫ���������豸�ĵ�ַ
	public static String getmDeviceAddress() {
		return mDeviceAddress;
	}

}
