package id.jagakeluarga.salesfunnel.ui.common

import androidx.compose.ui.graphics.Color
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline

/** Warna aksen per tahap pipeline, dipakai konsisten di seluruh app. */
fun warnaTahap(tahap: TahapPipeline): Color = when (tahap) {
    TahapPipeline.PROSPEK -> Color(0xFF6B7280)
    TahapPipeline.KUALIFIKASI -> Color(0xFF3B82F6)
    TahapPipeline.PRESENTASI -> Color(0xFF8B5CF6)
    TahapPipeline.PROPOSAL -> Color(0xFFF59E0B)
    TahapPipeline.CLOSING -> Color(0xFF10B981)
}
