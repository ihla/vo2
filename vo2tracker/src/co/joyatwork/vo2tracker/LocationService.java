package co.joyatwork.vo2tracker;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.google.android.gms.location.LocationClient;

import co.jaw.calculate.MotionMeter;
import co.joyatwork.vo2tracker.R;

public class LocationService extends IntentService {

	private static final String SERVICE_NAME = "Location Service";
	private String TAG = this.getClass().getSimpleName();
	private static final String LOGGER_FILE_NAME = "vo2.csv";
	private static final String LOGGER_FILE_HEADER = "lat,long,vo2";
	private CsvLogger logger;
	private final StringBuffer logString = new StringBuffer();
	
	public static class MotionMeterSingleton {
		private static MotionMeter instance = null;
		
		private static int age = 0;
		private static int height = 0;
		private static boolean isMale = false; 
		
		public static void set(int age, int height, boolean isMale) {
			MotionMeterSingleton.age = age;
			MotionMeterSingleton.height = height;
			MotionMeterSingleton.isMale = isMale;
		}
		
		public static MotionMeter getInstance() {
			if (instance == null) {
				instance = new MotionMeter(age, height, isMale);
				Log.i("MotionMeterSingleton", "new instance " + age + "," + height + "," + isMale);
			}
			return instance;
		}
		
		public static void deleteInstance() {
			instance = null; //freed for gc
		}
	}
	
	private MotionMeter motionMeter;
	public LocationService() {
		super(SERVICE_NAME);
	}
	
	public LocationService(String name) {
		super(SERVICE_NAME);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		logger = new CsvLogger(this);
		logger.createCsvFile(LOGGER_FILE_NAME, LOGGER_FILE_HEADER);
		motionMeter = MotionMeterSingleton.getInstance();
		
		//Log.i(TAG, "onCreate()");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		//Log.i(TAG, "onHandleIntent intent " + intent.toString());
		
		Location location = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);
		if(location != null) {
			long currentTimeMillis = System.currentTimeMillis();
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double vo2 = motionMeter.calculateVO2(latitude, longitude, currentTimeMillis); 
			
			logString.append(latitude)
 					.append(CsvLogger.CSV_DELIM)
 					.append(longitude)
					.append(CsvLogger.CSV_DELIM)
					.append(vo2);
			Log.i(TAG, "onHandleIntent log " + logString);
			logger.log(logString);
			
			showNotification(location, vo2);
			
		}
			
	}

	private void showNotification(Location location, double vo2) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
		Builder noti = new NotificationCompat.Builder(this);
		noti.setContentTitle("VO2 Track");
		noti.setContentText(String.format("%f, %f, %.2f", location.getLatitude(), location.getLongitude(), vo2));
		noti.setSmallIcon(R.drawable.ic_launcher);
		
		notificationManager.notify(1234, noti.build());
	}

}
