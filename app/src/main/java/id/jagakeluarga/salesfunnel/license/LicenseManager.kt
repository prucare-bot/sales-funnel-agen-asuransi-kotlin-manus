package id.jagakeluarga.salesfunnel.license

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Trial 5 hari + lisensi per-perangkat, tanpa server.
 *
 * Cara kerja kunci lisensi:
 * - Setiap HP punya ID unik (Android ID).
 * - Kunci = HMAC-SHA256(deviceId, SECRET) yang dipotong & diformat jadi kode
 *   16 karakter (mis. "A1B2-C3D4-E5F6-0789"). Karena SECRET cuma Anda yang
 *   tahu, kunci ini tidak bisa ditebak/dipakai untuk device lain.
 * - Untuk generate kunci pelanggan: minta device ID mereka (ditampilkan di
 *   layar aktivasi), lalu jalankan tools/generate_license_key.py dengan
 *   SECRET yang SAMA dengan yang ada di bawah ini.
 *
 * PENTING - keterbatasan yang jujur perlu diketahui:
 * 1. SECRET ini ada di dalam APK. Orang yang cukup ahli membongkar APK bisa
 *    menemukannya dan membuat generator sendiri. Ini risiko bawaan semua
 *    skema lisensi offline tanpa server; cukup untuk mencegah pemakaian
 *    kasual di luar pembeli, bukan proteksi tingkat militer.
 * 2. Anti-reset trial saat uninstall memakai file penanda tersembunyi di
 *    folder Downloads (folder publik, tidak ikut terhapus saat uninstall
 *    biasa). Ini best-effort: kalau pengguna sengaja menghapus file itu
 *    manual, trial akan reset. Tidak ada cara 100% mencegah ini tanpa server.
 */
object LicenseManager {
    private const val PREFS = "sales_funnel_license"
    private const val KEY_TRIAL_START = "trial_start_millis"
    private const val KEY_LICENSE_KEY = "license_key"
    private const val MARKER_FILENAME = ".sf_trial_marker.json"
    private const val TRIAL_DURATION_MILLIS = 5L * 24 * 60 * 60 * 1000

    // GANTI dengan string rahasia buatan Anda sendiri sebelum membagikan app ke
    // pelanggan (bebas, mis. kombinasi acak panjang). Nilai yang SAMA PERSIS
    // harus dipakai di tools/generate_license_key.py.
    private const val SECRET = "Sukses@2026"

    fun getDeviceId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN-DEVICE"

    /** Dipakai juga oleh tools/generate_license_key.py (logika HMAC harus identik). */
    fun expectedKeyFor(deviceId: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256"))
        val raw = mac.doFinal(deviceId.trim().uppercase().toByteArray())
        val hex = raw.joinToString("") { "%02X".format(it) }.take(16)
        return hex.chunked(4).joinToString("-")
    }

    fun isLicensed(context: Context): Boolean {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LICENSE_KEY, null)
            ?: return false
        return saved.equals(expectedKeyFor(getDeviceId(context)), ignoreCase = true)
    }

    /** Coba aktivasi dengan kunci yang dimasukkan pengguna. Mengembalikan true kalau valid & tersimpan. */
    fun activate(context: Context, inputKey: String): Boolean {
        val normalized = inputKey.trim().uppercase().replace("-", "").replace(" ", "")
        val expected = expectedKeyFor(getDeviceId(context)).replace("-", "")
        if (normalized.isNotEmpty() && normalized == expected) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LICENSE_KEY, expectedKeyFor(getDeviceId(context)))
                .apply()
            return true
        }
        return false
    }

    fun getTrialStart(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getLong(KEY_TRIAL_START, -1L)
        if (stored > 0) return stored

        val fromMarker = readMarker(context)
        val start = fromMarker ?: System.currentTimeMillis()
        prefs.edit().putLong(KEY_TRIAL_START, start).apply()
        if (fromMarker == null) writeMarker(context, start)
        return start
    }

    fun daysRemaining(context: Context): Int {
        val remainingMillis = TRIAL_DURATION_MILLIS - (System.currentTimeMillis() - getTrialStart(context))
        if (remainingMillis <= 0) return 0
        val fullDays = TimeUnit.MILLISECONDS.toDays(remainingMillis).toInt()
        val hasExtraHours = remainingMillis % (24 * 60 * 60 * 1000L) > 0
        return if (hasExtraHours) fullDays + 1 else fullDays
    }

    fun isTrialExpired(context: Context): Boolean =
        System.currentTimeMillis() - getTrialStart(context) >= TRIAL_DURATION_MILLIS

    /** Boleh dipakai kalau sudah punya lisensi valid ATAU masih dalam masa trial. */
    fun isAccessAllowed(context: Context): Boolean = isLicensed(context) || !isTrialExpired(context)

    private fun writeMarker(context: Context, startMillis: Long) {
        runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, MARKER_FILENAME)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Downloads.RELATIVE_PATH, "Download")
                }
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
            resolver.openOutputStream(uri)?.use { out ->
                out.write("""{"deviceId":"${getDeviceId(context)}","trialStart":$startMillis}""".toByteArray())
            }
        }
    }

    private fun readMarker(context: Context): Long? = runCatching {
        val resolver = context.contentResolver
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Downloads._ID),
            selection,
            arrayOf(MARKER_FILENAME),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                val uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
                resolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    val text = reader.readText()
                    if (text.contains(getDeviceId(context))) {
                        return@runCatching Regex(""""trialStart":(\d+)""").find(text)?.groupValues?.get(1)?.toLongOrNull()
                    }
                }
            }
            null
        }
    }.getOrNull()
}
