package com.jackytck.wifiscanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jackytck.signal.Measurement;
import com.jackytck.signal.MeasurementAdapter;
import com.jackytck.signal.WiFiReading;
import com.jackytck.signal.WifiSniffer;

public class MainActivity extends ActionBarActivity implements OnItemSelectedListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private ListFragment m_scan_list;
	private WifiSniffer m_wifi_service;
	//private Measurement m_last_measurement;
	private boolean m_recording = false;
	private Spinner m_spinner;
	private EditText m_edittext_location;
	private String m_selected_station = "";
	private Button m_button_start, m_button_stop;
	private CountDownTimer m_timmer;
	private FingerPrintDbAdapter m_db_adapter = new FingerPrintDbAdapter(this);

	/** 
     * {@link ServiceConnection} for the {@link WifiSniffer}
     */
	private ServiceConnection m_wifi_connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
        	m_wifi_service = ((WifiSniffer.LocalBinder) service).getService();
            //forceStartScanning();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        	m_wifi_service = null;
        }
    };
    
    /**
     * Receives notifications about new available measurements
     */
    private BroadcastReceiver m_wifi_receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Measurement m = m_wifi_service.retrieveLastMeasurement();
            Log.d(TAG, "onReceive()");
            
            if (m == null)
                return;
            
            MeasurementAdapter adapter = new MeasurementAdapter(MainActivity.this, m);
            m_scan_list.setListAdapter(adapter);
            //m_last_measurement = m;
            
            Vector <WiFiReading> results = m.getWiFiReadings();
            for(WiFiReading r: results) {
            	Log.d(TAG, r.getSsid()+": "+r.getBssid()+": "+r.getType()+": "+r.getRssi());
            	Log.d(TAG, "#################");
            	
            	if(m_recording) {
            		String location = m_edittext_location.getText().toString();
            		if(location.length() == 0)
            			location = m_selected_station;
            		m_db_adapter.insert(location, r.getSsid(), r.getBssid(), r.getRssi(), r.isWepEnabled(), r.isInfrastructure());
           
            	}
            }
            if(m_recording) {
            	String log = results.size() + " row";
            	if(results.size() == 1)
            		log += " is inserted";
            	else
            		log += "s are inserted";
	            Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
            }
            
            //m_wifi_service.stopMeasuring();
        }
    };

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//put greetings on the list first
		String[] greet = getResources().getStringArray(R.array.greeting_list);
		ArrayAdapter <String> adapter = new ArrayAdapter <String> (this, android.R.layout.simple_list_item_1, greet);
		m_scan_list = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.measurement_list);
		m_scan_list.setListAdapter(adapter);
		
		if (savedInstanceState == null) {
		}
		//startWifiSniffer();
		openDatabase();
		
		m_spinner = (Spinner) findViewById(R.id.spinner_station);
		m_spinner.setOnItemSelectedListener(this);
		
		m_edittext_location = (EditText) findViewById(R.id.input_location);
		m_edittext_location.addTextChangedListener(locationWatcher);
		
		m_button_start = (Button) findViewById(R.id.button_start);
		m_button_stop = (Button) findViewById(R.id.button_stop);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	}
	
	private TextWatcher locationWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (s.toString().length() == 0)
				m_button_start.setEnabled(false);
			else
				m_button_start.setEnabled(true);
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}
	};
	
	 private void openDatabase() {
    	if (m_db_adapter != null) {
    		m_db_adapter.open();
	    }
	}

    @Override
	protected void onPause() {
    	stopWifiSniffer();
    	super.onPause();
	}
    
    @Override
	protected void onResume() {
		super.onResume();
		startWifiSniffer();
	}
    
    @Override
	protected void onDestroy() {
    	stopWifiSniffer();
	    super.onDestroy();
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		if (id == R.id.action_export_csv) {
			if (!export_csv(getResources().getString(R.string.csv_filename)))
				Toast.makeText(getApplicationContext(), "Export failed!", Toast.LENGTH_LONG).show();
			return true;
		}
		if (id == R.id.action_share_csv) {
			share_csv(getResources().getString(R.string.csv_filename));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
     * Starts the sniffer and registers the receiver
     */
    private void startWifiSniffer() {
    	forceStartScanning();
        bindService(new Intent(this, WifiSniffer.class), m_wifi_connection, Context.BIND_AUTO_CREATE);
        registerReceiver(m_wifi_receiver, new IntentFilter(WifiSniffer.WIFI_ACTION));
        Log.i(TAG, "Started WifiSniffer");
    }

    /**
     * Stops the sniffer and unregisters the receiver
     */
    private void stopWifiSniffer() {
        if (m_wifi_service != null) {
        	m_wifi_service.stopMeasuring();
        }
        
        try {
        	unbindService(m_wifi_connection);
            unregisterReceiver(m_wifi_receiver);
            Log.i(TAG, "Stopped WifiSniffer");
        } 
        catch (IllegalArgumentException e) { 
        	/* Log it if you wish*/ 
        }
    }

    private void forceStartScanning() {
    	if(m_wifi_service != null) {
    		m_wifi_service.forceMeasurement();
    	}
    }
    
    public void startRecording(View view) {
    	startWifiSniffer();
		m_recording = true;
		view.setVisibility(View.GONE);
		m_button_stop.setVisibility(View.VISIBLE);
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Integer countdown = Integer.parseInt(sharedPref.getString("record_period", "30"));
		kickAutoStop(countdown);
	}
    
    public void kickAutoStop(Integer n) {
    	m_timmer = new CountDownTimer(n * 1000, 1000) {

			@Override
			public void onFinish() {
				stopRecording((View) m_button_stop);
			}

			@Override
			public void onTick(long ms) {
				m_button_stop.setText(getResources().getString(R.string.record_stop) + " (" + ms / 1000 + "s)");
			}
		}.start();
    }

    public void stopRecording(View view) {
    	stopWifiSniffer();
    	m_recording = false;
		m_timmer.cancel();
		view.setVisibility(View.GONE);
		m_button_start.setVisibility(View.VISIBLE);
	}
    
    public boolean export_csv(String filename) {
    	String csv = m_db_adapter.fetchAllInCSVString();
    	if(csv != "") {
            try {
            	File file = new File(getExternalFilesDir(null), filename);
                FileWriter out = new FileWriter(file);
                out.write(csv);
                out.close();
                Toast.makeText(getApplicationContext(), "Exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
            	Log.e("Exception", "File write failed: " + e.toString());
            	return false;
            }
    	}
    	else {
    		Toast.makeText(getApplicationContext(), "No data to export!", Toast.LENGTH_LONG).show();
    		return false;
    	}
    	return true;
    }
    
    public void share_csv(String filename) {
    	if (export_csv(filename)) {
    		File file = new File(getExternalFilesDir(null), filename);
        	if (file.exists()) {
        		Intent intent = new Intent(Intent.ACTION_SEND);
        		intent.setType("text/plain"); 
        		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file.getPath()));  
        		startActivity(Intent.createChooser(intent, getResources().getString(R.string.csv_share_via)));
        	}
    	}
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		m_selected_station = parent.getItemAtPosition(pos).toString();
		m_edittext_location.setText(m_selected_station);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}
}
