package id.jagakeluarga.salesfunnel.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.withTransaction
import id.jagakeluarga.salesfunnel.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AgendaReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AgendaReminderWorker.ACTION_COMPLETE) return
        val agendaId = intent.getStringExtra(AgendaReminderWorker.KEY_AGENDA_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val database = AppDatabase.getInstance(appContext)
                database.withTransaction {
                    database.agendaDao().getById(agendaId)?.let { agenda ->
                        database.agendaDao().upsert(agenda.copy(selesai = true))
                    }
                }
                AgendaScheduler.cancel(appContext, agendaId)
                val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.cancel(agendaId.hashCode())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
