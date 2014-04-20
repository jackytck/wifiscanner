package com.jackytck.signal;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jackytck.wifiscanner.SettingsActivity;

public class WifiSniffer extends Service {

	/** Action being broadcasted whenever a new measurement was retrieved */
	public static String WIFI_ACTION = "com.jackytck.signal.WIFI_ACTION";

	private static final String TAG = WifiManager.class.getSimpleName();

	WifiManager mWifi;
	Measurement mLastMeasurement;
	boolean stop = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		setup();
	}
	
	public void setup() {
		mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		registerReceiver(mStateReceiver, new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION));
		registerReceiver(mResultReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	@Override
	public void onDestroy() {
		stop = true;
		unregisterReceiver(mStateReceiver);
		unregisterReceiver(mResultReceiver);
		mWifi = null;
	}

	/**
	 * Start scanning immediately
	 */
	public void forceMeasurement() {
		stop = false;
		initiateSniff();
		Log.d(TAG, "forceMeasurement()");
	}

	/**
	 * Retrieve measurement from last scan.
	 * 
	 * @return {@link Measurement}
	 */
	public Measurement retrieveLastMeasurement() {
		return mLastMeasurement;
	}

	/**
	 * Stop scanning and broadcasting new measurements.
	 */
	public void stopMeasuring() {
		stop = true;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * @see Binder
	 */
	public class LocalBinder extends Binder {
		public WifiSniffer getService() {
			return WifiSniffer.this;
		}
	}

	private final LocalBinder mBinder = new LocalBinder();

	/**
	 * Try to enable the wifi adapter if it is not already enabled or not
	 * currently being enabled. Otherwise start the scanning directly.
	 */
	private void initiateSniff() {
		if (mWifi != null && !mWifi.isWifiEnabled()) {
			if (mWifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
				if (!mWifi.setWifiEnabled(true))
					return;
			}
		} else {
			Log.d(TAG, "initiateSniff:dispatchMessage()");
			mHandler.dispatchMessage(mHandler.obtainMessage(START_SCAN_MSG));
		}
	}

	/**
	 * {@link BroadcastReceiver} for reacting on the current wifi state. If the
	 * wifi adapter is enabled, start a scan.
	 */
	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mWifi.isWifiEnabled() && !stop)
				initiateSniff();
		}

	};

	/**
	 * {@link BroadcastReceiver} for retrieving scanning results and to creating
	 * a new {@link Measurement}. The measurement is then stored in
	 * {@link mLastMeasurement}.
	 */
	private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			//Do not listen to broadcast when not initiated scan
			//When no network is available, broadcasts are sent every few seconds
			if(stop) {
				Log.d(TAG, "Received not requested scan result");
				return;
			}
			
			List<ScanResult> results = mWifi.getScanResults();
			Measurement measurement = new Measurement();

			for (ScanResult result : results) {

				WiFiReading reading = new WiFiReading();
				reading.setBssid(result.BSSID);
				reading.setSsid(result.SSID);
				reading.setRssi(result.level);
				reading.setInfrastructure(result.capabilities
						.contains("[IBSS]"));
				reading.setWepEnabled(result.capabilities.contains("WEP")
						|| result.capabilities.contains("WPA")
						|| result.capabilities.contains("-EAP-"));

				measurement.addWiFiReading(reading);
			}

			mLastMeasurement = measurement;

			Intent wifiIntent = new Intent(WIFI_ACTION);
			sendBroadcast(wifiIntent);

		}

	};

	private static final int START_SCAN_MSG = 1;
	//private static final int SCAN_INTERVAL = 10;

	/**
	 * The {@link WifiSniffer.mHandler} is responsible for initiating the actual
	 * scanning and scheduling the next scan {@link SCAN_INTERVAL} seconds
	 * later.
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mWifi == null) {
				return;
			}
				
			if (stop) {
				Log.d(TAG, "Scanning stopped, skiping scan");
				return;
			}

			if (msg.what == START_SCAN_MSG) {
				Log.i(TAG, "Starting scan");
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(WifiSniffer.this);
				Integer interval = Integer.parseInt(sharedPref.getString("update_interval", "10"));
				
				mWifi.startScan();
				/*
				 * Remove any pending messages but make sure we are called back
				 * after SCAN_INTERVAL seconds
				 */
				removeMessages(START_SCAN_MSG);
				sendMessageDelayed(obtainMessage(START_SCAN_MSG),
						interval * 1000);
			} else {
				super.handleMessage(msg);
			}
		}
	};

}