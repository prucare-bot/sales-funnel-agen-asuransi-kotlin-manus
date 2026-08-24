package id.jagakeluarga.salesfunnel.whatsapp

data class WhatsAppTemplate(
    val title: String,
    val message: String,
)

object WhatsAppTemplates {
    val defaults = listOf(
        WhatsAppTemplate(
            title = "Follow-up umum",
            message = "Halo {nama}, saya ingin menindaklanjuti pembicaraan kita tentang kebutuhan perlindungan. Kapan waktu yang nyaman untuk berdiskusi?",
        ),
        WhatsAppTemplate(
            title = "Pengingat janji",
            message = "Halo {nama}, mengingatkan agenda {agenda} pada {waktu}. Sampai bertemu.",
        ),
        WhatsAppTemplate(
            title = "Kirim proposal",
            message = "Halo {nama}, saya sudah menyiapkan informasi/proposal yang kita bahas. Apakah boleh saya kirimkan sekarang?",
        ),
        WhatsAppTemplate(
            title = "Ucapan terima kasih",
            message = "Halo {nama}, terima kasih atas waktu dan kepercayaannya. Saya siap membantu jika ada pertanyaan lanjutan.",
        ),
    )

    fun render(template: WhatsAppTemplate, nama: String, agenda: String? = null, waktu: String? = null): String =
        template.message
            .replace("{nama}", nama)
            .replace("{agenda}", agenda ?: "follow-up")
            .replace("{waktu}", waktu ?: "waktu yang telah disepakati")
}
