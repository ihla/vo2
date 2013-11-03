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
import android.widget.RadioButton;
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
	private static final String KEY_INTERVAL = "co.joyatwork.vo2tracker.KEY_INTERVAL";
	private static final String KEY_ACCURACY = "co.joyatwork.vo2tracker.KEY_UPDATES_ACCURACY";
	private static final String KEY_AGE = "co.joyatwork.vo2tracker.KEY_AGE";
	private static final String KEY_HEIGHT = "co.joyatwork.vo2tracker.KEY_HEIGHT";
	private static final String KEY_MALE = "co.joyatwork.vo2tracker.KEY_MALE";

	private String TAG = this.getClass().getSimpleName();
	
	private TextView txtConnectionStatus;
	private EditText etLocationInterval;
	private TextView txtLocationRequest;
	
	private LocationClient locationclient;
	private LocationRequest locationrequest;
	private Intent intentService;
	private PendingIntent pendingIntent;
	
    SharedPreferences prefs;
    SharedPreferences.Editor prefsEditor;
    
	private boolean updatesRequested;
	private EditText ageEditText;
	private EditText heightEditText;
	private RadioButton radioButtonMale;
	private RadioButton radioButtonBalanced;
	private RadioButton radioButtonLow;
	private RadioButton radioButtonFemale;
	private RadioButton radioButtonHigh;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txtConnectionStatus = (TextView) findViewById(R.id.txtConnectionStatus);
		etLocationInterval = (EditText) findViewById(R.id.etLocationInterval);
		ageEditText = (EditText)findViewById(R.id.editTextAge);
		heightEditText = (EditText)findViewById(R.id.editTextHeight);
		radioButtonMale = (RadioButton)findViewById(R.id.radioMale);
		radioButtonFemale = (RadioButton)findViewById(R.id.radioFemale);
		radioButtonBalanced = (RadioButton)findViewById(R.id.radioBalanced);
		radioButtonHigh = (RadioButton)findViewById(R.id.radioHigh);
		radioButtonLow = (RadioButton)findViewById(R.id.radioLow);
		
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
		
		//Log.i(TAG, "onCreate()");
		
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
            updatesRequested = false;
        }
        
        if (updatesRequested) {
        	((Button)findViewById(R.id.btnRequestLocationIntent)).setText("Stop");
        }
        else {
        	((Button)findViewById(R.id.btnRequestLocationIntent)).setText("Start");
        }
        
        etLocationInterval.setText(String.valueOf(prefs.getLong(KEY_INTERVAL, 5000)));
        radioButtonBalanced.setChecked(isAccuracySetting(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY));
        radioButtonLow.setChecked(isAccuracySetting(LocationRequest.PRIORITY_LOW_POWER));
        radioButtonHigh.setChecked(isAccuracySetting(LocationRequest.PRIORITY_HIGH_ACCURACY));
        
        
        ageEditText.setText(String.valueOf(prefs.getInt(KEY_AGE, 30)));
        heightEditText.setText(String.valueOf(prefs.getInt(KEY_HEIGHT, 180)));
        radioButtonMale.setChecked(prefs.getBoolean(KEY_MALE, true));
        radioButtonFemale.setChecked(!prefs.getBoolean(KEY_MALE, true));
        
		//Log.i(TAG, "onResume()");

	}

	private boolean isAccuracySetting(int accuracyValue) {
		return accuracyValue == prefs.getInt(KEY_ACCURACY, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	}

	@Override
	protected void onPause() {
        // Save the current setting for updates
        prefsEditor.putBoolean(KEY_UPDATES_REQUESTED, updatesRequested);
        prefsEditor.putLong(KEY_INTERVAL, Long.parseLong(etLocationInterval.getText().toString()));
        prefsEditor.putInt(KEY_ACCURACY, getLocationUpdateAccuracy());
        prefsEditor.putInt(KEY_AGE, Integer.parseInt(ageEditText.getText().toString()));
        prefsEditor.putInt(KEY_HEIGHT, Integer.parseInt(heightEditText.getText().toString()));
        prefsEditor.putBoolean(KEY_MALE, radioButtonMale.isChecked());
        prefsEditor.commit();
        
		super.onPause();
		//Log.i(TAG, "onPause()");
	}


	public void buttonClicked(View v){
		if(v.getId() == R.id.btnRequestLocationIntent){
			if(((Button)v).getText().equals("Start")){
				
				locationrequest = LocationRequest.create();
				locationrequest.setPriority(getLocationUpdateAccuracy());
				locationrequest.setInterval(Long.parseLong(etLocationInterval.getText().toString()));
				
				LocationService.MotionMeterSingleton.deleteInstance();
				LocationService.MotionMeterSingleton.set(Integer.parseInt(ageEditText.getText().toString()), 
						Integer.parseInt(heightEditText.getText().toString()),
						radioButtonMale.isChecked());
				
				locationclient.requestLocationUpdates(locationrequest, pendingIntent);
				Log.i(TAG, "LocationRequest " + locationrequest.toString());

				((Button) v).setText("Stop");
				updatesRequested = true;
			}
			else{
				locationclient.removeLocationUpdates(pendingIntent);
				//Log.i(TAG, "stop intent " + pendingIntent.toString());

				((Button) v).setText("Start");
				updatesRequested = false;
			}
		}
	}

	private int getLocationUpdateAccuracy() {
		if (radioButtonBalanced.isChecked()) {
			return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
		}
		else if (radioButtonLow.isChecked()) {
			return LocationRequest.PRIORITY_LOW_POWER;
		}
		return LocationRequest.PRIORITY_HIGH_ACCURACY;
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(locationclient!=null)
			locationclient.disconnect();
		//Log.i(TAG, "onDestroy()");
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
