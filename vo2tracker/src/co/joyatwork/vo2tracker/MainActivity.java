package co.joyatwork.vo2tracker;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import co.joyatwork.vo2tracker.R;

public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener,LocationListener {

	private static final String SHARED_PREFERENCES = "co.joyatwork.vo2tracker.SHARED_PREFERENCES";
	private static final String KEY_UPDATES_REQUESTED = "co.joyatwork.vo2tracker.KEY_UPDATES_REQUESTED";

	private String TAG = this.getClass().getSimpleName();
	
	private TextView txtConnectionStatus;
	private TextView txtLastKnownLoc;
	private EditText etLocationInterval;
	private TextView txtLocationRequest;
	
	private LocationClient locationclient;
	private LocationRequest locationrequest;
	private Intent intentService;
	private PendingIntent pendingIntent;
	
    SharedPreferences prefs;
    SharedPreferences.Editor prefsEditor;

	private boolean updatesRequested;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txtConnectionStatus = (TextView) findViewById(R.id.txtConnectionStatus);
		txtLastKnownLoc = (TextView) findViewById(R.id.txtLastKnownLoc);
		etLocationInterval = (EditText) findViewById(R.id.etLocationInterval);
		txtLocationRequest = (TextView) findViewById(R.id.txtLocationRequest);
		
		intentService = new Intent(this,LocationService.class);
		pendingIntent = PendingIntent.getService(this, 1, intentService, 0);
		
        // Open Shared Preferences
        prefs = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        prefsEditor = prefs.edit();

        updatesRequested = false;

		int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resp == ConnectionResult.SUCCESS){
			locationclient = new LocationClient(this,this,this);
			locationclient.connect();
		}
		else{
			Toast.makeText(this, "Google Play Service Error " + resp, Toast.LENGTH_LONG).show();
			
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();

        // If the app already has a setting for getting location updates, get it
        if (prefs.contains(KEY_UPDATES_REQUESTED)) {
            updatesRequested = prefs.getBoolean(KEY_UPDATES_REQUESTED, false);

        // Otherwise, turn off location updates until requested
        } else {
            prefsEditor.putBoolean(KEY_UPDATES_REQUESTED, false);
            prefsEditor.commit();
        }
	}

	@Override
	protected void onPause() {
        // Save the current setting for updates
        prefsEditor.putBoolean(KEY_UPDATES_REQUESTED, updatesRequested);
        prefsEditor.commit();
        
		super.onPause();
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	
	public void buttonClicked(View v){
		if(v.getId() == R.id.btnLastLoc){
			if(locationclient!=null && locationclient.isConnected()){
				Location loc =locationclient.getLastLocation();
				Log.i(TAG, "Last Known Location :" + loc.getLatitude() + "," + loc.getLongitude());
				txtLastKnownLoc.setText(loc.getLatitude() + "," + loc.getLongitude());	
			}
		}
		if(v.getId() == R.id.btnStartRequest){
			if(locationclient!=null && locationclient.isConnected()){
				
				if(((Button)v).getText().equals("Start")){
					locationrequest = LocationRequest.create();
					locationrequest.setInterval(Long.parseLong(etLocationInterval.getText().toString()));
					locationclient.requestLocationUpdates(locationrequest, this);
					((Button) v).setText("Stop");	
				}
				else{
					locationclient.removeLocationUpdates(this);
					((Button) v).setText("Start");
				}
				
			}
		}
		if(v.getId() == R.id.btnRequestLocationIntent){
			if(((Button)v).getText().equals("Start")){
				
				locationrequest = LocationRequest.create();
				locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
				locationrequest.setInterval(Long.parseLong(etLocationInterval.getText().toString()));
				locationclient.requestLocationUpdates(locationrequest, pendingIntent);
				
				((Button) v).setText("Stop");
				updatesRequested = true;
			}
			else{
				locationclient.removeLocationUpdates(pendingIntent);
				((Button) v).setText("Start");
				updatesRequested = false;
			}
		}
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(locationclient!=null)
			locationclient.disconnect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "onConnected");
		txtConnectionStatus.setText("Connection Status : Connected");
		
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "onDisconnected");
		txtConnectionStatus.setText("Connection Status : Disconnected");
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "onConnectionFailed");
		txtConnectionStatus.setText("Connection Status : Fail");

	}

	@Override
	public void onLocationChanged(Location location) {
		if(location!=null){
			Log.i(TAG, "Location Request :" + location.getLatitude() + "," + location.getLongitude());
			txtLocationRequest.setText(location.getLatitude() + "," + location.getLongitude());
		}
		
	}

	

}
