package com.raya.app
import android.app.*
import android.content.*
import android.media.RingtoneManager
import android.os.*

class AlarmReceiver : BroadcastReceiver() {
  override fun onReceive(c: Context, i: Intent) {
    val l = i.getStringExtra("l") ?: "Alarm"
    val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nm.createNotificationChannel(NotificationChannel("al", "Alarms", NotificationManager.IMPORTANCE_HIGH))
    nm.notify(l.hashCode(), Notification.Builder(c, "al").setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("Raya").setContentText(l).build())
    val r = RingtoneManager.getRingtone(c, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
    r.play()
    Handler(Looper.getMainLooper()).postDelayed({ r.stop() }, 20000)
  }
}
