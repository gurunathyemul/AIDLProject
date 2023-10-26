package android.ptg.ptgsensor;

import android.content.Context;
import android.ptg.ptgsensor.IPTGSensorService;
import android.util.Log;

public class PTGSensorManager {
	private IPTGSensorService mService;
	public static final String TAG = "PTGSensorManager";

	public PTGSensorManager(Context ctx, IPTGSensorService service) {
		Log.d(TAG, "PTGSensorManager(ctx, service)");
		mService = service;
		Log.d(TAG, "Server Value: " + service);
	}

	public int getSensor(int sensor) {
		Log.d(TAG, "getSensor()");
		try {
			if (mService != null) {
				return mService.getSensor(sensor);
			} else {
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "getSensor(): Exception");
			return -1;
		}
	}


	public void registerListener(PTGSensorEventListener listener, int sensor, int rate) {
		Log.d(TAG, "registerListener() -> listener: " + listener + " sensor: " + sensor + " rate: " + rate);
		try {
			if (mService != null) {
				mService.registerListener(listener, sensor, rate);
			} else {
				Log.d(TAG, "registerListener: No Service connected");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "registerListener(): Exception");
		}
	}

	public void unregisterListener(int sensor) {
		Log.d(TAG, "unregisterListener() -> sensor: " + sensor);
		try {
			if (mService != null) {
				mService.unregisterListener(sensor);
			} else {
				Log.d(TAG, "unregisterListener: No Service connected");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "unregisterListener(): Exception");
		}
	}

}
