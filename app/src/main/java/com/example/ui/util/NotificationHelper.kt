package com.example.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object NotificationHelper {

    private const val CHANNEL_ID = "kivopay_transactions_channel"
    private const val CHANNEL_NAME = "Transaksi & Saldo KivoPay"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi transaksi masuk, transfer, QRIS, dan saldo KivoPay"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 120, 80, 200)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows an Android Heads-up banner notification at the top of the screen
     * and plays the custom synthesized metallic coin chime ("suara uang receh masuk").
     */
    fun showTransactionNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        initNotificationChannel(context)

        // 1. Play custom realistic coin chime sound asynchronously
        playCoinDropChime()

        // 2. Post heads-up system notification in Android header
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.kivopay_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 100, 60, 150))

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (_: Exception) {
            // Permission or context exception handled gracefully
        }
    }

    /**
     * Synthesizes a crisp, shimmering metallic coin drop chime sound
     * ("suara uang receh masuk" with authentic metallic resonance and dual-tone shimmer).
     */
    fun playCoinDropChime() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val durationMs = 420
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val audioBuffer = ShortArray(numSamples)

                // Harmonics of crisp silver coins: C7 (2093 Hz) + E7 (2637 Hz) + G7 (3136 Hz) + overtone shimmer
                val f1 = 2093.0
                val f2 = 2637.0
                val f3 = 3136.0
                val f4 = 4186.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Fast attack, natural exponential ringing decay
                    val envelope1 = Math.exp(-t * 9.5)
                    // Secondary coin bounce after 65ms
                    val bounceT = t - 0.065
                    val envelope2 = if (bounceT > 0) Math.exp(-bounceT * 12.0) * 0.7 else 0.0

                    val sample1 = (
                        sin(2.0 * Math.PI * f1 * t) * 0.45 +
                        sin(2.0 * Math.PI * f2 * t) * 0.35 +
                        sin(2.0 * Math.PI * f3 * t) * 0.20 +
                        sin(2.0 * Math.PI * f4 * t) * 0.12
                    ) * envelope1

                    val sample2 = if (bounceT > 0) {
                        (
                            sin(2.0 * Math.PI * (f1 * 1.12) * bounceT) * 0.4 +
                            sin(2.0 * Math.PI * (f2 * 1.15) * bounceT) * 0.3
                        ) * envelope2
                    } else 0.0

                    val totalSample = (sample1 + sample2).coerceIn(-1.0, 1.0)
                    audioBuffer[i] = (totalSample * Short.MAX_VALUE * 0.85).toInt().toShort()
                }

                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(numSamples * 2)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack(
                    audioAttributes,
                    audioFormat,
                    bufferSize,
                    AudioTrack.MODE_STATIC,
                    AudioManager.AUDIO_SESSION_ID_GENERATE
                )

                track.write(audioBuffer, 0, audioBuffer.size)
                track.play()

                // Keep track alive for playback duration
                Thread.sleep((durationMs + 80).toLong())
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio hardware error on restricted virtual devices
            }
        }
    }
}
