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
    // 1. Foreground Coin Offset
    private val _coinOffset = MutableStateFlow(Pair(0f, 0f))
    val coinOffset = _coinOffset.asStateFlow()

    // 2. Background Stars Offset (NEW)
    private val _bgOffset = MutableStateFlow(Pair(0f, 0f))
    val bgOffset = _bgOffset.asStateFlow()

    // Smoothing Variables (Current Positions)
    private var coinSmoothedX = 0f
    private var coinSmoothedY = 0f
    private var bgSmoothedX = 0f // NEW
    private var bgSmoothedY = 0f // NEW

    // CONFIGURATION
    private val SMOOTHING_FACTOR = 0.08f
    // Coin moves medium speed
    private val COIN_MULTIPLIER = 2.0f
    // Background moves INVERSE direction (negative sign) and SLOWER (smaller number)
    private val BG_MULTIPLIER = -0.8f

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

            // --- A. CALCULATE COIN PHYSICS (Foreground) ---
            // Invert rawX so tilting left slides left
            val coinTargetX = -rawX * COIN_MULTIPLIER
            val coinTargetY = rawY * COIN_MULTIPLIER

            // Apply Smoothing
            coinSmoothedX += (coinTargetX - coinSmoothedX) * SMOOTHING_FACTOR
            coinSmoothedY += (coinTargetY - coinSmoothedY) * SMOOTHING_FACTOR
            _coinOffset.value = Pair(coinSmoothedX, coinSmoothedY)


            // --- B. CALCULATE STAR PHYSICS (Background) ---
            // Use rawX directly (inverse of coin) and smaller multiplier for depth
            val bgTargetX = rawX * BG_MULTIPLIER
            // Invert rawY for background y-axis to oppose coin
            val bgTargetY = -rawY * BG_MULTIPLIER

            // Apply Smoothing to stars too
            bgSmoothedX += (bgTargetX - bgSmoothedX) * SMOOTHING_FACTOR
            bgSmoothedY += (bgTargetY - bgSmoothedY) * SMOOTHING_FACTOR
            _bgOffset.value = Pair(bgSmoothedX, bgSmoothedY)


            // --- C. CHEAT DETECTION ---
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