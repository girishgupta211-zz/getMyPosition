package com.example.getmyposition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.FloatMath;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements LocationListener {
	private GoogleMap map;
	private LocationManager locationManager;
	private String provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabledGPS = service
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// boolean enabledWiFi =
		// service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!enabledGPS) {
			Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG)
					.show();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		// Initialize the location fields
		/*
		 * if (location != null) { Toast.makeText(this, "Selected Provider " +
		 * provider, Toast.LENGTH_SHORT).show(); onLocationChanged(location); }
		 * else {
		 * 
		 * // do something }
		 */

	}

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 1000, 1, this);
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		/*
		 * lat = 12.58109; lng = 77.351421;
		 */
		ArrayList<String> wordList = new ArrayList<String>();

		map.clear(); // Remove all marker from map before refresh it.

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					getAssets().open("coordinates.txt")));
			String word;

			while ((word = br.readLine()) != null) {
				String[] data = word.split("-");
				System.out.println(data[2]);
				double dlat2 = Double.parseDouble(data[2]);
				double dlong2 = Double.parseDouble(data[3]);
				float lat2 = (float) dlat2;
				float long2 = (float) dlong2;
				boolean x = gps2m((float) lat, (float) lng, lat2, long2);
				if (x)
					wordList.add(word);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LatLng latLng = new LatLng(lat, lng);

/*		Toast.makeText(this, "Location " + lat + "," + lng, Toast.LENGTH_SHORT)
				.show();*/

		LatLng coordinate = new LatLng(lat, lng);
/*		Toast.makeText(this,
				"Location " + coordinate.latitude + "," + coordinate.longitude,
				Toast.LENGTH_SHORT).show();
*/
		Marker marker = map.addMarker(new MarkerOptions()
				.position(coordinate)
				.title("I need quick relief")
				.snippet("Plz help me!!!")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.current_position)));
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		for (int pin = 0; pin < wordList.size(); pin++) {

			String[] ncoordinates = wordList.get(pin).split("-");

			float nlatitude = Float.parseFloat(ncoordinates[2]);
			float nlongitude = Float.parseFloat(ncoordinates[3]);
			String address = ncoordinates[1] + "  " + ncoordinates[0];

			LatLng pinLocation = new LatLng(nlatitude, nlongitude);

			Marker storeMarker = map.addMarker(new MarkerOptions()
					.position(pinLocation)
					.title("Sulabh International")
					.snippet(address)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_launcher)));
		}

		// Zoom in the Google Map
		map.animateCamera(CameraUpdateFactory.zoomTo(14));

	}

	private Boolean gps2m(float lat_a, float lng_a, float lat_b, float lng_b) {
		float pk = (float) (180 / 3.14169);

		float a1 = lat_a / pk;
		float a2 = lng_a / pk;
		float b1 = lat_b / pk;
		float b2 = lng_b / pk;

		float t1 = FloatMath.cos(a1) * FloatMath.cos(a2) * FloatMath.cos(b1)
				* FloatMath.cos(b2);
		float t2 = FloatMath.cos(a1) * FloatMath.sin(a2) * FloatMath.cos(b1)
				* FloatMath.sin(b2);
		float t3 = FloatMath.sin(a1) * FloatMath.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);
		if (6366000 * tt < 700000)
			return true;
		else
			return false;
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
