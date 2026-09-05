package id.jagakeluarga.salesfunnel.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.jagakeluarga.salesfunnel.MainActivity

class AgendaReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_AGENDA_ID = "agenda_id"
        const val KEY_JUDUL = "judul"
        const val KEY_JENIS = "jenis"
        const val KEY_WAKTU_MULAI = "waktu_mulai"
        const val KEY_OFFSET_HOURS = "offset_hours"
        const val CHANNEL_ID = "agenda_reminders"
        const val ACTION_OPEN_AGENDA = "id.jagakeluarga.salesfunnel.OPEN_AGENDA"
        const val ACTION_COMPLETE = "id.jagakeluarga.salesfunnel.COMPLETE_AGENDA"
    }

    override suspend fun doWork(): Result {
        val agendaId = inputData.getString(KEY_AGENDA_ID) ?: id
        val judul = inputData.getString(KEY_JUDUL) ?: "Agenda"
        val jenis = inputData.getString(KEY_JENIS).orEmpty()
        val waktuMulai = inputData.getLong(KEY_WAKTU_MULAI, 0L)
        val offsetHours = inputData.getInt(KEY_OFFSET_HOURS, 24)
        val offsetLabel = if (offsetHours == 24) "1 hari sebelum janji" else "4 jam sebelum janji"

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Pengingat Agenda", NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Notifikasi follow-up prospek yang sudah dijadwalkan" }
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            action = ACTION_OPEN_AGENDA
            putExtra(KEY_AGENDA_ID, agendaId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            applicationContext,
            agendaId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val completeIntent = Intent(applicationContext, AgendaReminderActionReceiver::class.java).apply {
            action = ACTION_COMPLETE
            putExtra(KEY_AGENDA_ID, agendaId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            agendaId.hashCode() + 1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val waktuLabel = if (waktuMulai > 0) {
            java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale("id", "ID"))
                .format(java.util.Date(waktuMulai))
        } else "waktu yang telah dijadwalkan"
        val statusLabel = if (waktuMulai > 0 && waktuMulai < System.currentTimeMillis()) "Terlambat · " else ""
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(judul)
            .setContentText("$statusLabel$jenis · $waktuLabel · $offsetLabel")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tandai selesai", completePendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(agendaId.hashCode(), notification)
        return Result.success()
    }

}
