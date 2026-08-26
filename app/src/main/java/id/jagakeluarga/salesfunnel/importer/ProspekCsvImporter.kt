package id.jagakeluarga.salesfunnel.importer

import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline

object ProspekCsvImporter {
    data class Result(
        val valid: List<Prospek>,
        val errors: List<String>,
        val duplicates: List<String>,
    )

    fun parse(csv: String): Result {
        val rows = csv.lineSequence()
            .map { it.removePrefix("\uFEFF") }
            .filter { it.isNotBlank() }
            .map(::parseLine)
            .toList()
        if (rows.isEmpty()) return Result(emptyList(), listOf("File CSV kosong."), emptyList())
        val header = rows.first().map { normalize(it) }
        val nameIndex = findIndex(header, "nama", "name")
        if (nameIndex == -1) return Result(emptyList(), listOf("Kolom wajib Nama tidak ditemukan."), emptyList())
        val phoneIndex = findIndex(header, "no hpwa", "no hp", "nomor telepon", "nomor hp", "phone", "telepon")
        val emailIndex = findIndex(header, "email", "alamat email")
        val sourceIndex = findIndex(header, "sumber", "sumber prospek", "source")
        val stageIndex = findIndex(header, "tahap", "status", "status funnel", "stage")
        val premiumIndex = findIndex(header, "estimasi premi", "premi", "premium")
        val noteIndex = findIndex(header, "catatan", "notes", "note")
        val valid = mutableListOf<Prospek>()
        val errors = mutableListOf<String>()
        val duplicates = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        rows.drop(1).forEachIndexed { offset, row ->
            val line = offset + 2
            val nama = row.getOrNull(nameIndex)?.trim().orEmpty()
            if (nama.isBlank()) {
                errors += "Baris $line: Nama wajib diisi."
                return@forEachIndexed
            }
            val phone = row.valueAt(phoneIndex)?.trim()?.takeIf { it.isNotBlank() }
            val duplicateKey = phone?.filter(Char::isDigit)?.takeIf { it.isNotEmpty() } ?: nama.lowercase()
            if (!seen.add(duplicateKey)) {
                duplicates += "Baris $line: $nama"
                return@forEachIndexed
            }
            val tahapText = row.valueAt(stageIndex)?.trim().orEmpty()
            val tahap = parseStage(tahapText)
            if (tahapText.isNotBlank() && tahap == null) {
                errors += "Baris $line: Tahap funnel tidak dikenal: $tahapText."
                return@forEachIndexed
            }
            val premiText = row.valueAt(premiumIndex)?.trim().orEmpty()
            val premi = if (premiText.isBlank()) null else premiText.filter(Char::isDigit).toLongOrNull()
            if (premiText.isNotBlank() && premi == null) {
                errors += "Baris $line: Estimasi premi harus berupa angka."
                return@forEachIndexed
            }
            valid += Prospek(
                nama = nama,
                nomorTelepon = phone,
                email = row.valueAt(emailIndex)?.trim()?.takeIf { it.isNotBlank() },
                sumberProspek = row.valueAt(sourceIndex)?.trim()?.takeIf { it.isNotBlank() },
                tahap = tahap ?: TahapPipeline.PROSPEK,
                estimasiPremi = premi,
                catatan = row.valueAt(noteIndex)?.trim()?.takeIf { it.isNotBlank() },
            )
        }
        return Result(valid, errors, duplicates)
    }

    private fun findIndex(header: List<String>, vararg names: String): Int =
        header.indexOfFirst { it in names.map(::normalize) }

    private fun parseStage(value: String): TahapPipeline? =
        TahapPipeline.entries.firstOrNull { it.label.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) }

    private fun normalize(value: String): String = value.trim().lowercase().replace(Regex("[^a-z0-9]"), "")

    private fun List<String>.valueAt(index: Int): String? = getOrNull(index)

    private fun parseLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var quoted = false
        var index = 0
        while (index < line.length) {
            when (val char = line[index]) {
                '"' -> if (quoted && index + 1 < line.length && line[index + 1] == '"') {
                    current.append('"')
                    index++
                } else quoted = !quoted
                ',' -> if (quoted) current.append(char) else {
                    values += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            index++
        }
        values += current.toString()
        return values
    }
}
