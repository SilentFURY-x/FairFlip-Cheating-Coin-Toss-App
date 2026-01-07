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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.random.Random

class CoinViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    // --- SENSORS & HAPTICS SYSTEM ---
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Vibrator (Supports old and new Android versions)
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // --- STATE VARIABLES ---

    // 1. Parallax Offset (For the "Floating" effect)
    // We emit X and Y coordinates to move the coin slightly
    private val _coinOffset = MutableStateFlow(Pair(0f, 0f))
    val coinOffset = _coinOffset.asStateFlow()

    // 2. Cheat Logic
    private var isStealthModeOn = false // If true, cheats are disabled (Safe mode)
    private var currentCheatState = CheatState.RANDOM // The current rigged outcome
    private var lastVibratedState = CheatState.RANDOM // To prevent constant buzzing

    enum class CheatState {
        HEADS, // Rigged for Heads
        TAILS, // Rigged for Tails
        RANDOM // Fair flip
    }

    // --- INIT ---
    init {
        // Start listening to sensors immediately
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    // --- SENSOR LOGIC ---
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0] // Tilt Left/Right
            val y = event.values[1] // Tilt Up/Down

            // A. PARALLAX EFFECT (Floating Coin)
            // We invert values so if you tilt left, coin slides left (gravity)
            // Multiplier determines how "heavy" the coin feels.
            val parallaxX = -x * 3f
            val parallaxY = y * 3f
            _coinOffset.value = Pair(parallaxX, parallaxY)

            // B. CHEAT DETECTION
            if (!isStealthModeOn) {
                detectCheatTilt(x)
            }
        }
    }

    private fun detectCheatTilt(xValue: Float) {
        // Threshold: How much you need to tilt to trigger the cheat
        val tiltThreshold = 3.0f

        val newState = when {
            xValue > tiltThreshold -> CheatState.HEADS // Tilted Left (usually positive X on some devices, verify!)
            xValue < -tiltThreshold -> CheatState.TAILS // Tilted Right
            else -> CheatState.RANDOM // Flat(ish)
        }

        if (newState != currentCheatState) {
            currentCheatState = newState
            // Haptic Feedback to confirm cheat selection
            if (newState != lastVibratedState) {
                triggerHaptic(newState)
                lastVibratedState = newState
            }
        }
    }

    // --- HAPTIC FEEDBACK ENGINE ---
    private fun triggerHaptic(state: CheatState) {
        if (!vibrator.hasVibrator()) return

        val effect = when (state) {
            CheatState.HEADS -> {
                // Light Tick (1 short pulse)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createOneShot(50, 100)
                }
            }
            CheatState.TAILS -> {
                // Heavy Double Tick
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createWaveform(longArrayOf(0, 70, 50, 70), -1)
                } else {
                    @Suppress("DEPRECATION")
                    VibrationEffect.createWaveform(longArrayOf(0, 70, 50, 70), -1)
                }
            }
            CheatState.RANDOM -> null // No vibration when returning to flat
        }

        effect?.let { vibrator.vibrate(it) }
    }

    // --- PUBLIC ACTIONS ---

    fun toggleStealthMode(): String {
        isStealthModeOn = !isStealthModeOn
        // Reset cheat state when entering stealth
        if (isStealthModeOn) {
            currentCheatState = CheatState.RANDOM
            lastVibratedState = CheatState.RANDOM
        }
        return if (isStealthModeOn) "/// STEALTH MODE ACTIVE ///" else "/// CHEAT MODE READY ///"
    }

    fun getFlipResult(currentHeadsState: Boolean): Boolean {
        return when (currentCheatState) {
            CheatState.HEADS -> true // Force Heads
            CheatState.TAILS -> false // Force Tails
            CheatState.RANDOM -> !currentHeadsState // Standard Toggle (Fair)
            // Or true random: Random.nextBoolean()
        }
    }

    // Cleanup
    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}