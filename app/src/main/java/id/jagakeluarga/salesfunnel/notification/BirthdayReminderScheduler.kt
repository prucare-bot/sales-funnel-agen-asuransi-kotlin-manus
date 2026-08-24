package id.jagakeluarga.salesfunnel.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import java.util.Calendar
import java.util.concurrent.TimeUnit

object BirthdayReminderScheduler {
    private fun uniqueName(nasabahId: String) = "birthday_reminder_$nasabahId"

    fun schedule(context: Context, nasabah: Nasabah) {
        val birthday = nasabah.tanggalLahir ?: return
        val now = Calendar.getInstance()
        val reminder = Calendar.getInstance().apply {
            timeInMillis = birthday
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -1)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.YEAR, 1)
        }
        val delay = reminder.timeInMillis - System.currentTimeMillis()
        if (delay <= 0) return
        val request = OneTimeWorkRequestBuilder<BirthdayReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                BirthdayReminderWorker.KEY_ID to nasabah.id,
                BirthdayReminderWorker.KEY_NAME to nasabah.nama,
                BirthdayReminderWorker.KEY_PHONE to nasabah.nomorTelepon.orEmpty(),
                BirthdayReminderWorker.KEY_BIRTHDAY to birthday,
            ))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(uniqueName(nasabah.id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, nasabahId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(nasabahId))
    }
}
