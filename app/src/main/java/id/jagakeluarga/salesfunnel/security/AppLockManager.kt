package id.jagakeluarga.salesfunnel.security

import android.content.Context
import java.security.MessageDigest

object AppLockManager {
    private const val PREFS = "sales_funnel_security"
    private const val PIN_HASH = "pin_hash"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(PIN_HASH)

    fun setPin(context: Context, pin: String): Boolean {
        if (pin.length < 4 || pin.any { !it.isDigit() }) return false
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(PIN_HASH, hash(pin))
            .apply()
        return true
    }

    fun verifyPin(context: Context, pin: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(PIN_HASH, null) == hash(pin)

    fun clearPin(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(PIN_HASH)
            .apply()
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
