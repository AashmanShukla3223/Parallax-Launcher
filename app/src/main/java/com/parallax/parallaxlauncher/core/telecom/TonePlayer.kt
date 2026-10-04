package com.parallax.parallaxlauncher.core.telecom

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.ToneGenerator
import com.parallax.parallaxlauncher.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class TonePlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    } catch (_: Exception) {
        null
    }

    private var helloMotoPlayer: MediaPlayer? = null

    private val sampleRate = 44100

    fun playDtmf(digit: Char) {
        // Try hardware ToneGenerator first
        val tgSuccess = try {
            val tone = when (digit) {
                '0' -> ToneGenerator.TONE_DTMF_0
                '1' -> ToneGenerator.TONE_DTMF_1
                '2' -> ToneGenerator.TONE_DTMF_2
                '3' -> ToneGenerator.TONE_DTMF_3
                '4' -> ToneGenerator.TONE_DTMF_4
                '5' -> ToneGenerator.TONE_DTMF_5
                '6' -> ToneGenerator.TONE_DTMF_6
                '7' -> ToneGenerator.TONE_DTMF_7
                '8' -> ToneGenerator.TONE_DTMF_8
                '9' -> ToneGenerator.TONE_DTMF_9
                '*' -> ToneGenerator.TONE_DTMF_S
                '#' -> ToneGenerator.TONE_DTMF_P
                else -> -1
            }
            if (tone != -1 && toneGenerator != null) {
                toneGenerator?.startTone(tone, 150)
                true
            } else false
        } catch (_: Exception) {
            false
        }

        // Also synthesize PCM dual-frequency sine wave to guarantee playback on emulators
        playPcmDtmf(digit)
    }

    private fun playPcmDtmf(digit: Char) {
        val (f1, f2) = when (digit) {
            '1' -> 697 to 1209
            '2' -> 697 to 1336
            '3' -> 697 to 1477
            '4' -> 770 to 1209
            '5' -> 770 to 1336
            '6' -> 770 to 1477
            '7' -> 852 to 1209
            '8' -> 852 to 1336
            '9' -> 852 to 1477
            '*' -> 941 to 1209
            '0', '+' -> 941 to 1336
            '#' -> 941 to 1477
            else -> 1000 to 1000
        }
        playDualTone(f1.toDouble(), f2.toDouble(), 140)
    }

    /** Iconic Motorola RAZR high-frequency UI chirp */
    fun playRazrChirp() {
        playDualTone(1200.0, 2400.0, 50)
    }

    fun playRingback() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
        } catch (_: Exception) {}
        playDualTone(440.0, 480.0, 750)
    }

    fun playBusy() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_BUSY, 400)
        } catch (_: Exception) {}
        playDualTone(480.0, 620.0, 350)
    }

    private fun playDualTone(freq1: Double, freq2: Double, durationMs: Int) {
        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Envelope: attack & decay to prevent clicking
                    val envelope = when {
                        i < 150 -> i / 150.0
                        i > numSamples - 150 -> (numSamples - i) / 150.0
                        else -> 1.0
                    }
                    val sample = ((sin(2.0 * PI * freq1 * t) + sin(2.0 * PI * freq2 * t)) * 0.48 * envelope * Short.MAX_VALUE).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val minSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(2048)

                val bufferSizeBytes = maxOf(minSize, buffer.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                kotlinx.coroutines.delay(durationMs.toLong() + 40)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                android.util.Log.e("TonePlayer", "Error playing tone: ${e.message}", e)
            }
        }
    }

    fun playHelloMoto(context: Context) {
        scope.launch(Dispatchers.Main) {
            try {
                helloMotoPlayer?.stop()
                helloMotoPlayer?.release()
                helloMotoPlayer = MediaPlayer.create(context.applicationContext, R.raw.hello_moto)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setOnCompletionListener {
                        it.release()
                        if (helloMotoPlayer === it) helloMotoPlayer = null
                    }
                    start()
                }
            } catch (e: Exception) {
                android.util.Log.e("TonePlayer", "Error playing hello moto: ${e.message}", e)
            }
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
        try {
            helloMotoPlayer?.stop()
            helloMotoPlayer?.release()
            helloMotoPlayer = null
        } catch (_: Exception) {}
    }
}
