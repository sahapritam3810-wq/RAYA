package com.raya.app
import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

class KeepAliveService : Service() {
  override fun onBind(i: Intent?): IBinder? = null
  override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    nm.createNotificationChannel(NotificationChannel("k", "Raya listening", NotificationManager.IMPORTANCE_LOW))
    val n = Notification.Builder(this, "k").setSmallIcon(android.R.drawable.ic_btn_speak_now)
      .setContentTitle("Raya is listening").setContentText("Say: Hey Raya").build()
    if (Build.VERSION.SDK_INT >= 29) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
    else startForeground(1, n)
    return START_STICKY
  }
}
