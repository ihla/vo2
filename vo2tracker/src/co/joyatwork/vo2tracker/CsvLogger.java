package co.joyatwork.vo2tracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

public class CsvLogger {

    public static final char CSV_DELIM = ',';
	private static final String TAG = "CsvLogger";

	private Context context;
	private PrintWriter serviceLogWriter;
	private StringBuffer serviceLogString;

	public CsvLogger(Context context) {
		this.context = context;
	}
	
	/**
	 * rememebre to set 
	 *     '<'uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	 * @param fileName - e.g. "log.csv"
	 * @param fileHeader - e.g. "val1,val2,val3"
	 */
	public void createCsvFile(String fileName, String fileHeader) {
		File serviceLogFile = new File(context.getExternalCacheDir(), fileName);
		//Log.i(TAG, serviceLogFile.getAbsolutePath());
		try {
			//FileWriter calls directly OS for every write request
			//For better performance the FileWriter is wrapped into BufferedWriter, which calls out for a batch of bytes
			//PrintWriter allows a human-readable writing into a file
			if (serviceLogFile.exists()) {
				serviceLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(serviceLogFile, true)));
			}
			else {
				serviceLogWriter = new PrintWriter(new BufferedWriter(new FileWriter(serviceLogFile)));
				serviceLogWriter.println("time," + fileHeader);
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not open file", e);
		}
		
		serviceLogString = new StringBuffer();
	}

	public void log(StringBuffer latLong) {
		if (serviceLogWriter != null) {
			String timeStamp = DateFormat.format("hh:mm:ssaa dd/MM/yyyy", System.currentTimeMillis()).toString();
			serviceLogString.delete(0, serviceLogString.length())
				.append(timeStamp).append(CSV_DELIM)
				.append(latLong)
				;
			serviceLogWriter.println(serviceLogString.toString());
			if (serviceLogWriter.checkError()) {
				Log.e(TAG, "Error writing  log");
			}
		}
		else { 
			Log.e(TAG, "writter null");
		}
	}

}
