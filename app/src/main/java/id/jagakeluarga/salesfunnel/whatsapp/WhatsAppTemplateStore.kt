package id.jagakeluarga.salesfunnel.whatsapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Menyimpan template WhatsApp yang bisa diedit pengguna, terpisah dari
 * WhatsAppTemplates.defaults (dipakai sebagai isi awal saat pertama kali
 * dibuka, atau kalau pengguna menghapus semua templatenya).
 */
object WhatsAppTemplateStore {
    private const val PREF_NAME = "sales_funnel_settings"
    private const val KEY_TEMPLATES = "whatsapp_templates_json"

    fun load(context: Context): List<WhatsAppTemplate> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_TEMPLATES, null) ?: return WhatsAppTemplates.defaults
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                WhatsAppTemplate(title = obj.getString("title"), message = obj.getString("message"))
            }
        }.getOrDefault(WhatsAppTemplates.defaults).ifEmpty { WhatsAppTemplates.defaults }
    }

    fun save(context: Context, templates: List<WhatsAppTemplate>) {
        val array = JSONArray()
        templates.forEach { template ->
            array.put(
                JSONObject().apply {
                    put("title", template.title)
                    put("message", template.message)
                },
            )
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TEMPLATES, array.toString())
            .apply()
    }
}
