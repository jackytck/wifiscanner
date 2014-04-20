package com.jackytck.signal;

import java.util.Vector;

public class Measurement {


	/* time of measurement */
	protected long timestamp = 0;

	/* set of GSM readings that where taken during the measurement */
	/* cant use generic type because of compatibility to j2me which does not have an generic vector type */
	//protected Vector gsmReadings;

	/* set of WiFi readings that where taken during the measurement */
	protected Vector<WiFiReading> wifiReadings;

	/* set of Bluetooth readings that where taken during the measurement */
	//protected Vector bluetoothReadings;

	/* constructor */
	public Measurement() {
		timestamp = System.currentTimeMillis();
		//gsmReadings = new Vector();
		wifiReadings = new Vector<WiFiReading>();
		//bluetoothReadings = new Vector();
		
	}
	
	public Measurement(Vector<WiFiReading> wifiReadings) {
		timestamp = System.currentTimeMillis();
		//this.gsmReadings = gsmReadings;
		this.wifiReadings = wifiReadings;
		//this.bluetoothReadings = bluetoothReadings;
	}


	/* ************ Getter and Setter Methods ************ */

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the gsm readings
	 
	public Vector getGsmReadings() {
		return gsmReadings;
	}

	public void addGSMReading(GSMReading gsmReading) {
		this.gsmReadings.addElement(gsmReading);
	}
	*/

	/**
	 * @return the wifi readings
	 */
	public Vector<WiFiReading> getWiFiReadings() {
		return wifiReadings;
	}

	public void addWiFiReading(WiFiReading wiFiReading) {
		this.wifiReadings.addElement(wiFiReading);
	}
	
	public String[] getNames() {
		String[] ret = new String[wifiReadings.size()];
		
		for(int i=0; i<wifiReadings.size(); i++) {
			ret[i] = wifiReadings.get(i).getSsid();
		}
		
		return ret;
	}
	
	public Vector <WiFiReading> getTrustedReadings() {
		Vector <WiFiReading> ret = new Vector <WiFiReading>();
		
		for(WiFiReading r: wifiReadings) {
			ret.add(r);
		}
		
		return ret;
	}

	/**
	 * @return the bluetooth readings
	 
	public Vector getBluetoothReadings() {
		return bluetoothReadings;
	}

	public void addBluetoothReading(BluetoothReading bluetoothReading) {
		this.bluetoothReadings.addElement(bluetoothReading);
	}
	*/
}
