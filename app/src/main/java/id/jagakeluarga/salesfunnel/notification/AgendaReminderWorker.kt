package id.jagakeluarga.salesfunnel.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class AgendaReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_JUDUL = "judul"
        const val KEY_JENIS = "jenis"
        const val CHANNEL_ID = "agenda_reminders"
    }

    override suspend fun doWork(): Result {
        val judul = inputData.getString(KEY_JUDUL) ?: "Agenda"
        val jenis = inputData.getString(KEY_JENIS).orEmpty()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Pengingat Agenda", NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Notifikasi follow-up prospek yang sudah dijadwalkan" }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(judul)
            .setContentText(jenis)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(id.hashCode(), notification)
        return Result.success()
    }
}
