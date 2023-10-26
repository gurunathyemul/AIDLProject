package android.ptg.ptgsensor;

import android.ptg.ptgsensor.IPTGSensorEventListener;

interface IPTGSensorService {
    int getSensor(int value);
    void registerListener(IPTGSensorEventListener listener, int sensor, int rate);
    void unregisterListener(int sensor);
    boolean stop();
}