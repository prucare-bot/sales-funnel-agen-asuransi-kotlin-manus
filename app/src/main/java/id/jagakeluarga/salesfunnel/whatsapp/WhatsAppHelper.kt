package id.jagakeluarga.salesfunnel.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WhatsAppHelper {
    fun openChat(context: Context, phone: String?, message: String) {
        val normalized = normalizeIndonesianNumber(phone)
        if (normalized == null) {
            Toast.makeText(context, "Nomor HP/WA belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$normalized?text=$encodedMessage"))
        context.startActivity(intent)
    }

    private fun normalizeIndonesianNumber(phone: String?): String? {
        val digits = phone.orEmpty().filter(Char::isDigit)
        if (digits.isBlank()) return null
        return when {
            digits.startsWith("62") -> digits
            digits.startsWith("0") -> "62${digits.drop(1)}"
            else -> digits
        }
    }
}
