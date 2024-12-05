package com.lorinta.newproject

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import okhttp3.*
import java.io.IOException
import android.app.AlarmManager
import android.content.Context
import java.util.*
import android.content.pm.PackageManager

private val discordWebhookURL = "https://discord.com/api/webhook URL"

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val targetLocation = Location("provider").apply {
        latitude = 34.49734106 // ここがざひょう (e.g., Tokyo Station)
        longitude = 136.69874556
    }
    private val targetRadius = 3000 // 500 meters
    private val lineNotifyToken = "line notify token" // Set your LINE Notify token here

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize location client and request
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationRequest = LocationRequest.create().apply {
                interval = 2000 // 10 seconds
                fastestInterval = 2000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation ?: return
                    handleLocationUpdate(location)
                }
            }

            // Start location updates
            startLocationUpdates()

            // Start foreground notification
            startForeground(1, createNotification())
        } catch (e: Exception) {
            sendErrorViaLineNotify("Location service error: ${e.message}")
        }

        setDailyAlarm()
    }

    private fun setDailyAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Set alarm for 8 AM
        val alarmIntentAM = Intent(this, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val calendarAM = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendarAM.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntentAM
        )

        // Set alarm for 8 PM
        val alarmIntentPM = Intent(this, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val calendarPM = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendarPM.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntentPM
        )
    }

    private fun sendAliveNotification() {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("content", "起動してるよ")
            .build()
        val request = Request.Builder()
            .url(discordWebhookURL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                // Do nothing, message sent successfully
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "SEND_ALIVE_NOTIFICATION") {
            sendAliveNotification()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendNotification(message: String, distance: Float) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("content", "<@1230420481686110242> $message: 距離 $distance メートルです。")
            .build()
        val request = Request.Builder()
            .url(discordWebhookURL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                sendErrorViaDiscord("Failed to send notification via Discord: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                // Do nothing, message sent successfully
            }
        })
    }

    private fun startLocationUpdates() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // Handle the case where permissions are not granted
            sendErrorViaLineNotify("Location permissions are not granted")
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "LocationServiceChannel"
        val channel = NotificationChannel(
            notificationChannelId,
            "Location Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("アプリサービス")
            .setContentText("デバッグアプリが起動中です")
            .setSmallIcon(android.R.drawable.ic_dialog_map) // Use built-in drawable
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun handleLocationUpdate(location: Location) {
        val distance = location.distanceTo(targetLocation)
        if (distance <= targetRadius) {
            sendNotification("近づきました", distance)
        }
    }


    private fun sendErrorViaLineNotify(error: String) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("message", "Error: $error")
            .build()
        val request = Request.Builder()
            .url("https://notify-api.line.me/api/notify")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $lineNotifyToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                // Do nothing, error sent successfully
            }
        })
    }

    private fun sendErrorViaDiscord(error: String) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("content", "Error: $error")
            .build()
        val request = Request.Builder()
            .url(discordWebhookURL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                // Do nothing, error sent successfully
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
