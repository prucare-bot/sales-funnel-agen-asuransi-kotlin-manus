package id.jagakeluarga.salesfunnel.security

import android.content.Context
import java.security.MessageDigest

object AppLockManager {
    private const val PREFS = "sales_funnel_security"
    private const val PIN_HASH = "pin_hash"
    private const val FAILED_ATTEMPTS = "failed_attempts"
    private const val LOCKED_UNTIL = "locked_until"
    private const val BACKGROUND_AT = "background_at"
    private const val MAX_ATTEMPTS = 5
    private const val LOCKOUT_MILLIS = 30_000L
    private const val AUTO_LOCK_MILLIS = 5 * 60 * 1000L

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(PIN_HASH)

    fun setPin(context: Context, pin: String): Boolean {
        if (pin.length < 4 || pin.any { !it.isDigit() }) return false
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(PIN_HASH, hash(pin))
            .remove(FAILED_ATTEMPTS)
            .remove(LOCKED_UNTIL)
            .apply()
        return true
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lockedUntil = prefs.getLong(LOCKED_UNTIL, 0L)
        if (lockedUntil > now) return false
        if (prefs.getString(PIN_HASH, null) == hash(pin)) {
            prefs.edit().remove(FAILED_ATTEMPTS).remove(LOCKED_UNTIL).apply()
            return true
        }
        val attempts = prefs.getInt(FAILED_ATTEMPTS, 0) + 1
        if (attempts >= MAX_ATTEMPTS) {
            prefs.edit().putInt(FAILED_ATTEMPTS, 0).putLong(LOCKED_UNTIL, now + LOCKOUT_MILLIS).apply()
        } else {
            prefs.edit().putInt(FAILED_ATTEMPTS, attempts).apply()
        }
        return false
    }

    fun remainingLockoutSeconds(context: Context): Long =
        ((context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(LOCKED_UNTIL, 0L) - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)

    fun markBackground(context: Context) {
        if (isEnabled(context)) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong(BACKGROUND_AT, System.currentTimeMillis())
                .apply()
        }
    }

    fun shouldAutoLock(context: Context): Boolean {
        if (!isEnabled(context)) return false
        val backgroundAt = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(BACKGROUND_AT, 0L)
        return backgroundAt > 0L && System.currentTimeMillis() - backgroundAt >= AUTO_LOCK_MILLIS
    }

    fun clearBackgroundMarker(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(BACKGROUND_AT).apply()
    }

    fun clearPin(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(PIN_HASH)
            .remove(FAILED_ATTEMPTS)
            .remove(LOCKED_UNTIL)
            .remove(BACKGROUND_AT)
            .apply()
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
