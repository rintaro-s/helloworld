package com.lorinta.newproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, LocationService::class.java)
        serviceIntent.action = "SEND_ALIVE_NOTIFICATION"
        context.startService(serviceIntent)
    }
}