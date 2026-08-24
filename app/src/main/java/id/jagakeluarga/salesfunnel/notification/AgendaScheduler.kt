package id.jagakeluarga.salesfunnel.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import java.util.concurrent.TimeUnit

object AgendaScheduler {
    private fun uniqueName(agendaId: String) = "agenda_reminder_$agendaId"

    /** Menjadwalkan (atau menjadwal ulang) notifikasi untuk satu agenda. */
    fun schedule(context: Context, agenda: Agenda) {
        val delay = agenda.waktuMulai - System.currentTimeMillis()
        if (agenda.selesai || delay <= 0) {
            cancel(context, agenda.id)
            return
        }
        val data = workDataOf(
            AgendaReminderWorker.KEY_JUDUL to agenda.judul,
            AgendaReminderWorker.KEY_JENIS to agenda.jenis.label,
        )
        val request = OneTimeWorkRequestBuilder<AgendaReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(agenda.id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, agendaId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(agendaId))
    }
}
