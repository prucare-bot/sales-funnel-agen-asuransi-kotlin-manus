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
    private const val REMINDER_HOUR = 9
    private const val SAME_DAY_GRACE_MINUTES = 30L

    private fun uniqueName(nasabahId: String) = "birthday_reminder_$nasabahId"

    fun schedule(context: Context, nasabah: Nasabah) {
        val birthday = nasabah.tanggalLahir ?: return
        val reminder = nextReminderTime(birthday)
        val delay = reminder.timeInMillis - System.currentTimeMillis()
        if (delay <= 0) return
        enqueue(context, nasabah, birthday, reminder, delay)
    }

    /**
     * Menjadwalkan pengingat hari-H bila aplikasi dibuka pada hari ulang tahun.
     * Jika sudah lewat jam pengingat, notifikasi dibuat sesegera mungkin agar tidak
     * menunggu satu tahun penuh.
     */
    fun scheduleIfBirthdayToday(context: Context, nasabah: Nasabah) {
        val birthday = nasabah.tanggalLahir ?: return
        val now = Calendar.getInstance()
        val birthdayThisYear = birthdayInYear(birthday, now.get(Calendar.YEAR))
        val isToday = sameMonthAndDay(birthdayThisYear, now)
        if (!isToday) {
            schedule(context, nasabah)
            return
        }

        val reminder = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (now.get(Calendar.HOUR_OF_DAY) < REMINDER_HOUR) {
                set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
                set(Calendar.MINUTE, 0)
            } else {
                timeInMillis += SAME_DAY_GRACE_MINUTES * 60 * 1000
            }
        }
        val delay = (reminder.timeInMillis - System.currentTimeMillis()).coerceAtLeast(1_000L)
        enqueue(context, nasabah, birthday, reminder, delay)
    }

    private fun nextReminderTime(birthday: Long): Calendar {
        val now = Calendar.getInstance()
        val birthdayThisYear = birthdayInYear(birthday, now.get(Calendar.YEAR))
        val reminder = (birthdayThisYear.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -1)
        }
        if (reminder.timeInMillis <= now.timeInMillis) {
            val nextYearBirthday = birthdayInYear(birthday, now.get(Calendar.YEAR) + 1)
            return (nextYearBirthday.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -1)
            }
        }
        return reminder
    }

    private fun birthdayInYear(birthday: Long, year: Int): Calendar = Calendar.getInstance().apply {
        timeInMillis = birthday
        set(Calendar.YEAR, year)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun sameMonthAndDay(first: Calendar, second: Calendar): Boolean =
        first.get(Calendar.MONTH) == second.get(Calendar.MONTH) &&
            first.get(Calendar.DAY_OF_MONTH) == second.get(Calendar.DAY_OF_MONTH)

    private fun enqueue(
        context: Context,
        nasabah: Nasabah,
        birthday: Long,
        reminder: Calendar,
        delay: Long,
    ) {
        val data = workDataOf(
            BirthdayReminderWorker.KEY_ID to nasabah.id,
            BirthdayReminderWorker.KEY_NAME to nasabah.nama,
            BirthdayReminderWorker.KEY_PHONE to nasabah.nomorTelepon.orEmpty(),
            BirthdayReminderWorker.KEY_BIRTHDAY to birthday,
            BirthdayReminderWorker.KEY_IS_TODAY to birthdayInYear(birthday, Calendar.getInstance().get(Calendar.YEAR))
                .let { sameMonthAndDay(it, Calendar.getInstance()) },
        )
        val request = OneTimeWorkRequestBuilder<BirthdayReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName(nasabah.id),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel(context: Context, nasabahId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(nasabahId))
    }
}
