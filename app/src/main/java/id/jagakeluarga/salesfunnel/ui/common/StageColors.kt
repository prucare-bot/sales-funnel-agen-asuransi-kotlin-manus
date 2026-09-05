package id.jagakeluarga.salesfunnel.ui.common

import androidx.compose.ui.graphics.Color
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline

/**
 * Warna aksen per tahap pipeline, dipakai konsisten di seluruh app.
 * Redesain 2026: gradasi dingin -> hangat -> hijau sukses, agar corong terasa
 * "memanas" saat prospek mendekati closing. Lihat mockup redesign-semua-layar.html.
 */
fun warnaTahap(tahap: TahapPipeline): Color = when (tahap) {
    TahapPipeline.PROSPEK -> Color(0xFF8D93A6)
    TahapPipeline.KUALIFIKASI -> Color(0xFF4C86A8)
    TahapPipeline.PRESENTASI -> Color(0xFF6B5CA5)
    TahapPipeline.PROPOSAL -> Color(0xFFE4A335)
    TahapPipeline.CLOSING -> Color(0xFF3F8F5F)
}
