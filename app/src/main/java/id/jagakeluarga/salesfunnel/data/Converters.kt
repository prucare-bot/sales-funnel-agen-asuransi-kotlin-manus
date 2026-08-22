package id.jagakeluarga.salesfunnel.data

import androidx.room.TypeConverter
import id.jagakeluarga.salesfunnel.data.entity.JenisAgenda
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline

class Converters {
    @TypeConverter
    fun fromTahap(value: TahapPipeline): String = value.name

    @TypeConverter
    fun toTahap(value: String): TahapPipeline = TahapPipeline.valueOf(value)

    @TypeConverter
    fun fromJenisAgenda(value: JenisAgenda): String = value.name

    @TypeConverter
    fun toJenisAgenda(value: String): JenisAgenda = JenisAgenda.valueOf(value)
}
