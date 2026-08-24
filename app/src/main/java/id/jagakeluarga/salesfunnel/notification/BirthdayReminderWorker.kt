package id.jagakeluarga.salesfunnel.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import java.util.Calendar

class BirthdayReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    companion object {
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_PHONE = "phone"
        const val KEY_BIRTHDAY = "birthday"
        const val KEY_IS_TODAY = "is_today"
        private const val CHANNEL_ID = "birthday_reminders"
    }

    override suspend fun doWork(): Result {
        val name = inputData.getString(KEY_NAME) ?: "Nasabah"
        val nasabahId = inputData.getString(KEY_ID) ?: "worker-$name"
        val birthday = inputData.getLong(KEY_BIRTHDAY, 0L)
        val isToday = inputData.getBoolean(KEY_IS_TODAY, false)
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Pengingat Ulang Tahun", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val notificationText = if (isToday) {
            "Hari ini ulang tahun $name. Saat yang tepat untuk menyapa."
        } else {
            "Besok ulang tahun $name. Saat yang tepat untuk menyapa."
        }
        manager.notify(nasabahId.hashCode(), NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Ulang tahun nasabah")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build())

        BirthdayReminderScheduler.schedule(
            applicationContext,
            Nasabah(
                id = nasabahId,
                nama = name,
                nomorTelepon = inputData.getString(KEY_PHONE),
                produk = "",
                tanggalLahir = birthday,
            ),
        )
        return Result.success()
    }
}
