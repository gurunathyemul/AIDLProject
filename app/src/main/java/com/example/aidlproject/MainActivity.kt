package com.example.aidlproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.ptg.ptgsensor.Context
import android.ptg.ptgsensor.PTGSensor
import android.ptg.ptgsensor.PTGSensorEvent
import android.ptg.ptgsensor.PTGSensorEventListener
import android.ptg.ptgsensor.PTGSensorManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aidlproject.databinding.ActivityMainBinding
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : AppCompatActivity() {
    private var magnetometerValues: FloatArray? = null
    private var accelerometerValues: FloatArray? = null
    var threadPool: ExecutorService = Executors.newFixedThreadPool(10)
    var uiHandler = Handler(Looper.getMainLooper())

    private var magnetoSensor: Int = 0
    private var accSensor: Int = 0
    private lateinit var ptgSensorManager: PTGSensorManager
    private lateinit var binding: ActivityMainBinding
    val ameerpetLatLng = LatLng(17.4375, 78.4482)
    val KothagudaLatLng = LatLng(17.4643, 78.3756)
    val tcsLat = LatLng(17.444826231193, 78.37781581990357)
    val durgamCheruvuLatLng = LatLng(17.4300, 78.3895)
    val gachibowliLatLng = LatLng(17.439942546312206, 78.34882434530869)
    val ikeaLatLng = LatLng(17.4424122448374, 78.3780579478865)
    val cyberTowerLatLng = LatLng(17.45077048554629, 78.38119090225709)
    val ptgLatLng = LatLng(17.448632032825227, 78.37596994760975)
    private var position: Int = 0
    private var secondLatLng: LatLng = ameerpetLatLng


    val latLngArray = arrayOf(
        ameerpetLatLng,
        KothagudaLatLng,
        tcsLat,
        durgamCheruvuLatLng,
        gachibowliLatLng,
        ikeaLatLng,
        cyberTowerLatLng
    )
    val latLngNames = arrayOf(
        "AmeerpetLatLng",
        "KothagudaLatLng",
        "TCSLatLng",
        "DurgamCheruvuLatLng",
        "GachibowliLatLng",
        "IkeaLatLng",
        "CyberTowerLatLng",
    )
    private val timer = Timer()

    private val timerTask = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                if (latLngArray.size != position) {
                    secondLatLng = latLngArray[position]
                    binding.tvLatLngName.text = latLngNames[position]
                    position++
                } else position = 0
                binding.tvSecLatLng.text = "${secondLatLng.latitude}::${secondLatLng.longitude}"
            }
        }
    }


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main) as ActivityMainBinding
        timer.schedule(timerTask, 50, 6000)
        ptgSensorManager = (getSystemService(Context.PTGSENSOR_SERVICE) as PTGSensorManager?)!!
        initUI()
    }

    private fun initUI() {

        accSensor = ptgSensorManager.getSensor(PTGSensor.TYPE_ACCELEROMETER)
        Log.d("MainActivity", "accSensor: $accSensor")
        magnetoSensor = ptgSensorManager.getSensor(PTGSensor.TYPE_MAGNETOMETER)
        Log.d("MainActivity", "magnetoSensor: $magnetoSensor")
        binding.btnGachibowli.setOnClickListener {
            secondLatLng = gachibowliLatLng
        }
        binding.btnIkea.setOnClickListener {
            secondLatLng = ikeaLatLng
        }
        binding.btnCyberTowers.setOnClickListener {
            secondLatLng = cyberTowerLatLng
        }
    }

    private var ptgSensorEventListener = object : PTGSensorEventListener() {
        @Throws(RemoteException::class)
        override fun onSensorData(event: PTGSensorEvent) {
            if (event.sensor == accSensor) {
                // Handle acc sensor data here
                threadPool.execute {
                    Log.d(
                        "SensorData",
                        "Accelerometer: X: " + event.values[0] + " Y: " + event.values[1] + " Z: "
                                + event.values[2]
                    ) // Add this log message
                    uiHandler.post {
                        val acc = String.format(
                            "X: %.2f Y: %.2f Z: %.2f",
                            event.values[0], event.values[1],
                            event.values[2]
                        )
                    }
                    accelerometerValues = event.values

                }
            } else if (event.sensor == magnetoSensor) {
                // Handle magnetometer data here
                threadPool.execute {
                    Log.d(
                        "SensorData",
                        "Magnetometer: X: " + event.values[0] + " Y: " + event.values[1] + " Z: "
                                + event.values[2] + " D: " + event.values[3]
                    ) // Add this log message
                    uiHandler.post {
                        val magneto = String.format(
                            "X: %.2f Y: %.2f Z: %.2f D: %.2f",
                            event.values[0],
                            event.values[1], event.values[2], event.values[3]
                        )
                    }
                    magnetometerValues = event.values
                }
            }
            if (accelerometerValues != null && magnetometerValues != null) {
                val rotationMatrix = FloatArray(9)
                val success =
                    getRotMatrix(rotationMatrix, null, accelerometerValues!!, magnetometerValues!!)
                Log.d(TAG, "success:$success ")
                if (success) {
                    val orientationValues = FloatArray(3)
                    val getOrientationArray = getOrientation(rotationMatrix, orientationValues)
                    Log.d(TAG, "getOrientationArray:$getOrientationArray")
                    val azimuthDegrees = Math.toDegrees(orientationValues[0].toDouble())
                        .toFloat() + 110 //remove 11  0(used for different devices)
                    // Calculate the bearing (azimuth) from your current location to the second LatLng
//                currentLocation?.let {
                    val bearing = calculateBearing(
//                        currentLocation!!.latitude,
//                        currentLocation!!.longitude,
                        ptgLatLng.latitude,
                        ptgLatLng.longitude,
                        secondLatLng.latitude,
                        secondLatLng.longitude
                    )
                    // Calculate the relative direction
                    val relativeDirection = calculateRelativeDirection(bearing, azimuthDegrees)
                    // Display the relative direction in your UI (e.g., TextView)
                    updateUIWithDirection(bearing, relativeDirection, azimuthDegrees)
                }
            }
        }
    }

    private fun calculateBearing(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val deltaLng = endLng - startLng
        val y = sin(deltaLng) * cos(endLat)
        val x = cos(startLat) * sin(endLat) - sin(startLat) * cos(endLat) * cos(deltaLng)
        val initialBearing = atan2(y, x)
        return ((Math.toDegrees(initialBearing) + 360) % 360).toFloat()
    }

    private fun calculateRelativeDirection(bearing: Float, azimuth: Float): String {
        // Calculate the relative direction based on the difference between bearing and azimuth
        val angleDifference = (bearing - azimuth + 360) % 360
            binding.ivNav.rotation=angleDifference
        return when {
            angleDifference >= 337.5 || angleDifference < 22.5 -> "N"
            angleDifference >= 22.5 && angleDifference < 67.5 -> "NE"
            angleDifference >= 67.5 && angleDifference < 112.5 -> "E"
            angleDifference >= 112.5 && angleDifference < 157.5 -> "SE"
            angleDifference >= 157.5 && angleDifference < 202.5 -> "S"
            angleDifference >= 202.5 && angleDifference < 247.5 -> "SW"
            angleDifference >= 247.5 && angleDifference < 292.5 -> "W"
            angleDifference >= 292.5 && angleDifference < 337.5 -> "NW"
            else -> "N"
        }
    }

    private fun updateUIWithDirection(bearing: Float, direction: String, azimuthDegrees: Float) {
        uiHandler.post {
            binding.angleDirection.text = "$azimuthDegrees"
        }
    }

    fun getOrientation(R: FloatArray, values: FloatArray): FloatArray? {
        /*
         * 4x4 (length=16) case:
         *   /  R[ 0]   R[ 1]   R[ 2]   0  \
         *   |  R[ 4]   R[ 5]   R[ 6]   0  |
         *   |  R[ 8]   R[ 9]   R[10]   0  |
         *   \      0       0       0   1  /
         *
         * 3x3 (length=9) case:
         *   /  R[ 0]   R[ 1]   R[ 2]  \
         *   |  R[ 3]   R[ 4]   R[ 5]  |
         *   \  R[ 6]   R[ 7]   R[ 8]  /
         *
         */
        if (R.size == 9) {
            values[0] = Math.atan2(R[1].toDouble(), R[4].toDouble()).toFloat()
            values[1] = Math.asin(-R[7].toDouble()).toFloat()
            values[2] = Math.atan2(-R[6].toDouble(), R[8].toDouble()).toFloat()
        } else {
            values[0] = Math.atan2(R[1].toDouble(), R[5].toDouble()).toFloat()
            values[1] = Math.asin(-R[9].toDouble()).toFloat()
            values[2] = Math.atan2(-R[8].toDouble(), R[10].toDouble()).toFloat()
        }
        return values
    }

    fun getRotMatrix(
        R: FloatArray?, I: FloatArray?,
        gravity: FloatArray, geomagnetic: FloatArray
    ): Boolean {
        // TODO: move this to native code for efficiency
        var Ax = gravity[0]
        var Ay = gravity[1]
        var Az = gravity[2]
        val normsqA = Ax * Ax + Ay * Ay + Az * Az
        val g = 9.81f
        val freeFallGravitySquared = 0.01f * g * g
        if (normsqA < freeFallGravitySquared) {
            // gravity less than 10% of normal value
            return false
        }
        val Ex = geomagnetic[0]
        val Ey = geomagnetic[1]
        val Ez = geomagnetic[2]
        var Hx = Ey * Az - Ez * Ay
        var Hy = Ez * Ax - Ex * Az
        var Hz = Ex * Ay - Ey * Ax
        val normH = Math.sqrt((Hx * Hx + Hy * Hy + Hz * Hz).toDouble()).toFloat()
        if (normH < 0.1f) {
            // device is close to free fall (or in space?), or close to
            // magnetic north pole. Typical values are  > 100.
            return false
        }
        val invH = 1.0f / normH
        Hx *= invH
        Hy *= invH
        Hz *= invH
        val invA = 1.0f / Math.sqrt((Ax * Ax + Ay * Ay + Az * Az).toDouble()).toFloat()
        Ax *= invA
        Ay *= invA
        Az *= invA
        val Mx = Ay * Hz - Az * Hy
        val My = Az * Hx - Ax * Hz
        val Mz = Ax * Hy - Ay * Hx
        if (R != null) {
            if (R.size == 9) {
                R[0] = Hx
                R[1] = Hy
                R[2] = Hz
                R[3] = Mx
                R[4] = My
                R[5] = Mz
                R[6] = Ax
                R[7] = Ay
                R[8] = Az
            } else if (R.size == 16) {
                R[0] = Hx
                R[1] = Hy
                R[2] = Hz
                R[3] = 0f
                R[4] = Mx
                R[5] = My
                R[6] = Mz
                R[7] = 0f
                R[8] = Ax
                R[9] = Ay
                R[10] = Az
                R[11] = 0f
                R[12] = 0f
                R[13] = 0f
                R[14] = 0f
                R[15] = 1f
            }
        }
        if (I != null) {
            // compute the inclination matrix by projecting the geomagnetic
            // vector onto the Z (gravity) and X (horizontal component
            // of geomagnetic vector) axes.
            val invE = 1.0f / Math.sqrt((Ex * Ex + Ey * Ey + Ez * Ez).toDouble()).toFloat()
            val c = (Ex * Mx + Ey * My + Ez * Mz) * invE
            val s = (Ex * Ax + Ey * Ay + Ez * Az) * invE
            if (I.size == 9) {
                I[0] = 1f
                I[1] = 0f
                I[2] = 0f
                I[3] = 0f
                I[4] = c
                I[5] = s
                I[6] = 0f
                I[7] = -s
                I[8] = c
            } else if (I.size == 16) {
                I[0] = 1f
                I[1] = 0f
                I[2] = 0f
                I[4] = 0f
                I[5] = c
                I[6] = s
                I[8] = 0f
                I[9] = -s
                I[10] = c
                I[14] = 0f
                I[13] = I[14]
                I[12] = I[13]
                I[11] = I[12]
                I[7] = I[11]
                I[3] = I[7]
                I[15] = 1f
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        if (accSensor > 0) {
            threadPool.execute {
                Log.d(
                    "MainActivity",
                    "registerListener() -> listener: $ptgSensorEventListener accSensor: $accSensor"
                )
                ptgSensorManager.registerListener(
                    ptgSensorEventListener, accSensor,
                    PTGSensor.SENSOR_DELAY_FASTEST
                )
            }
        }
        if (magnetoSensor > 0) {
            threadPool.execute {
                Log.d(
                    "MainActivity",
                    "registerListener() -> listener: " + ptgSensorEventListener + " magnetoSensor: "
                            + magnetoSensor
                )
                ptgSensorManager.registerListener(
                    ptgSensorEventListener, magnetoSensor,
                    PTGSensor.SENSOR_DELAY_FAST
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}