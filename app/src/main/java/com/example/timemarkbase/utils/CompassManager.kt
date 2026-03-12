package com.example.timemarkbase.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class CompassManager(
    context: Context,
    private val callback: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private var isRunning = false

    fun start() {
        if (isRunning) return
        if (accelerometer == null || magnetometer == null) {
            Log.w("CompassManager", "Device missing required sensors!")
            return
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        isRunning = true
        Log.d("CompassManager", "CompassManager started")
    }

    fun stop() {
        if (!isRunning) return
        sensorManager.unregisterListener(this)
        isRunning = false
        Log.d("CompassManager", "CompassManager stopped")
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity.copyInto(event.values)
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic.copyInto(event.values)
        }

        // Chỉ tính khi cả hai mảng có dữ liệu
        if (gravity.any { it != 0f } && geomagnetic.any { it != 0f }) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                val azimuth = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
                Log.d("CompassManager", "Bearing: $azimuth")
                callback(azimuth)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Optional: có thể log accuracy nếu muốn debug
    }
}