package com.example.applocalnotification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MyApp: Application() {
    companion object{
        const val CHANNEL_ID = "my_channel"
    }
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Actualizar de Producto",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Este ecanala se usa para mostrar actualizacion del producto"
            val notificationManager = this.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}