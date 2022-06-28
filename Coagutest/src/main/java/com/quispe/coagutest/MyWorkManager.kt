package com.quispe.coagutest

import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.quispe.coagutest.database.room.Control
import com.quispe.coagutest.ui.common.clearSeconds
import com.quispe.coagutest.ui.common.fromDate
import com.quispe.coagutest.ui.mainActiviy.MainActivity
import java.util.*
import java.util.concurrent.TimeUnit


//https://www.youtube.com/watch?v=7XwkR80EdG0
class MyWorkManager(context: Context, workerParamaters: WorkerParameters): Worker(context, workerParamaters) {
    override fun doWork(): Result {
        val valor = inputData.getString("valor")

        if(shouldShowNotification(applicationContext))
            createNotification(valor!!)

        return Result.success()
    }

    fun shouldShowNotification(context: Context): Boolean {
        val myProcess = RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(myProcess)
        if (myProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND) return true
        val km =
            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        // app is in foreground, but if screen is locked show notification anyway
        return km.inKeyguardRestrictedInputMode()
    }

    companion object {
        private const val TAG = "ALARMA_NOTIFICATION"

        fun updateNotification(duracion: Long, data: Data) {
            WorkManager.getInstance().cancelAllWorkByTag(TAG)
            val not = OneTimeWorkRequest.Builder(MyWorkManager::class.java).setInitialDelay(duracion, TimeUnit.MILLISECONDS).addTag(TAG).setInputData(data).build()
            val instance = WorkManager.getInstance()
            instance.enqueue(not)
        }

        fun clearAllWorks() {
            WorkManager.getInstance().cancelAllWorkByTag(TAG)
        }

        // Creamos los trabajos programados
        fun setWorkers(controls: List<Control>, hourAlarm: Int, minuteAlarm: Int) {
            clearAllWorks()

            if(controls.count() == 0)
                return

            val workers: ArrayList<WorkRequest> = arrayListOf()

            for (control in controls) {
                val notificationTime = Calendar.getInstance().fromDate(control.executionDate, hourAlarm, minuteAlarm).timeInMillis - System.currentTimeMillis().clearSeconds()
                val dataParams = Data.Builder().putString("valor", control.resource).build()
                val worker = OneTimeWorkRequest.Builder(MyWorkManager::class.java).setInitialDelay(notificationTime, TimeUnit.MILLISECONDS).addTag(TAG).setInputData(dataParams).build()
                workers.add(worker)
            }
            val instance = WorkManager.getInstance()
            instance.enqueue(workers)
        }
    }

    fun createNotification(valor: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val NOTIFICATION_CHANNEL_ID = "my_channel_id_01"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "My Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            // Configure the notification channel.
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager!!.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setTicker("Hearty365") //     .setPriority(Notification.PRIORITY_MAX)
            .setContentTitle(applicationContext.resources.getString(com.quispe.coagutest.R.string.title_notificacion))
            .setContentText(String.format(applicationContext.resources.getString(R.string.msg_notificacion), valor))
            .setContentIntent(pendingIntent)
            .setContentInfo("Info")

        notificationManager!!.notify( /*notification id*/1, notificationBuilder.build())
    }

}