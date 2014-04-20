package com.jackytck.signal;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jackytck.wifiscanner.R;

public class MeasurementAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private Measurement measurement;

	public MeasurementAdapter(Activity context, Measurement m) {
		super(context, R.layout.fingerprint_rowlayout, m.getNames());
		this.context = context;
		this.measurement = m;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.fingerprint_rowlayout, null, true);
		TextView ssid = (TextView) rowView.findViewById(R.id.fingerprint_ssid);
		TextView address = (TextView) rowView.findViewById(R.id.fingerprint_address);
		TextView rss = (TextView) rowView.findViewById(R.id.fingerprint_rss);
		TextView wep = (TextView) rowView.findViewById(R.id.fingerprint_wep);
		TextView infas = (TextView) rowView.findViewById(R.id.fingerprint_infas);
		
		String s = measurement.getWiFiReadings().get(position).getSsid();
		ssid.setText(s);
		
		s = measurement.getWiFiReadings().get(position).getBssid();
		address.setText(s);
		
		s = "" + measurement.getWiFiReadings().get(position).getRssi();
		rss.setText(s);
		
		s = "" + measurement.getWiFiReadings().get(position).isWepEnabled();
		wep.setText(s);
		
		s = "" + measurement.getWiFiReadings().get(position).isInfrastructure();
		infas.setText(s);

		return rowView;
	}
}
