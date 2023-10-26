package android.ptg.ptgsensor;

import android.os.Parcel;
import android.os.Parcelable;

public class PTGSensorEvent implements Parcelable {
    public int sensor; // Use int instead of int8_t
    public float[] values; // Use List<Float> instead of vec<float>

    public PTGSensorEvent(int sensor, float[] values) {
        this.sensor = sensor;
        this.values = values;
    }

    protected PTGSensorEvent(Parcel in) {
        sensor = in.readInt();
        // Read the float array correctly
        values = new float[in.readInt()];
        in.readFloatArray(values);
    }

    public static final Creator<PTGSensorEvent> CREATOR = new Creator<PTGSensorEvent>() {
        @Override
        public PTGSensorEvent createFromParcel(Parcel in) {
            return new PTGSensorEvent(in);
        }

        @Override
        public PTGSensorEvent[] newArray(int size) {
            return new PTGSensorEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sensor);
        // Write the length of the float array first
        dest.writeInt(values.length);
        dest.writeFloatArray(values);
    }
}
