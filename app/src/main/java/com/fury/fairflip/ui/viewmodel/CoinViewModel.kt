package com.fury.fairflip.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class CoinViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // --- STATE VARIABLES ---
    private val _coinOffset = MutableStateFlow(Pair(0f, 0f))
    val coinOffset = _coinOffset.asStateFlow()

    // Smoothing Variables (Current Position)
    private var smoothedX = 0f
    private var smoothedY = 0f

    // CONFIGURATION
    // 0.1f = Very Smooth/Slow (Heavy feel). 0.5f = Snappy.
    private val SMOOTHING_FACTOR = 0.5f
    // Reduced from 3f to 2f for less aggressive movement
    private val SENSITIVITY_MULTIPLIER = 4.0f

    private var isStealthModeOn = false
    private var currentCheatState = CheatState.RANDOM
    private var lastVibratedState = CheatState.RANDOM

    enum class CheatState { HEADS, TAILS, RANDOM }

    init {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val rawX = event.values[0]
            val rawY = event.values[1]

            // --- 1. SMOOTHING ALGORITHM (Low Pass Filter) ---
            // Target is where the sensor WANTS to be.
            // We only move a small percentage (SMOOTHING_FACTOR) towards it per frame.

            // Invert rawX so tilting left slides left
            val targetX = -rawX * SENSITIVITY_MULTIPLIER
            val targetY = rawY * SENSITIVITY_MULTIPLIER

            // Formula: New = Old + (Target - Old) * Alpha
            smoothedX += (targetX - smoothedX) * SMOOTHING_FACTOR
            smoothedY += (targetY - smoothedY) * SMOOTHING_FACTOR

            // Emit the buttery smooth values
            _coinOffset.value = Pair(smoothedX, smoothedY)

            // --- 2. CHEAT DETECTION (Use Raw Data for accuracy) ---
            // We use raw data for cheats because we want instant trigger response,
            // even if the visual movement is slow/smooth.
            if (!isStealthModeOn) {
                detectCheatTilt(rawX)
            }
        }
    }

    private fun detectCheatTilt(xValue: Float) {
        val tiltThreshold = 5.0f

        val newState = when {
            xValue > tiltThreshold -> CheatState.HEADS
            xValue < -tiltThreshold -> CheatState.TAILS
            else -> CheatState.RANDOM
        }

        if (newState != currentCheatState) {
            currentCheatState = newState
            if (newState != lastVibratedState) {
                triggerHaptic(newState)
                lastVibratedState = newState
            }
        }
    }

    private fun triggerHaptic(state: CheatState) {
        if (!vibrator.hasVibrator()) return

        val effect = when (state) {
            CheatState.HEADS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(100, 255)
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createOneShot(100, 255)
                }
            }
            CheatState.TAILS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 50, 150), intArrayOf(0, 255, 0, 255), -1)
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 50, 150), -1)
                }
            }
            CheatState.RANDOM -> null
        }
        effect?.let { vibrator.vibrate(it) }
    }

    fun toggleStealthMode(): String {
        isStealthModeOn = !isStealthModeOn
        if (isStealthModeOn) {
            currentCheatState = CheatState.RANDOM
            lastVibratedState = CheatState.RANDOM
        }
        return if (isStealthModeOn) "/// STEALTH MODE ACTIVE ///" else "/// CHEAT MODE READY ///"
    }

    fun getFlipResult(currentHeadsState: Boolean): Boolean {
        return when (currentCheatState) {
            CheatState.HEADS -> true
            CheatState.TAILS -> false
            CheatState.RANDOM -> Random.nextBoolean()
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}