package android.ptg.ptgsensor;

import android.ptg.ptgsensor.PTGSensorEvent;

oneway interface IPTGSensorEventListener {
    void  onSensorData (in PTGSensorEvent event);
}
