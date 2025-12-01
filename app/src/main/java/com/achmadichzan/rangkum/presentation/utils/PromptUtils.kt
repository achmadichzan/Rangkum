package com.achmadichzan.rangkum.presentation.utils

object PromptUtils {
    fun create(transcript: String): String {
        return """
            Berikut adalah transkrip mentah dari speech-to-text yang mungkin mengandung typo karena kemiripan suara:
            
            "$transcript"
            
            Instruksi Khusus:
            1. Pahami konteks dan perbaiki kesalahan ejaan/tata bahasa secara internal.
            2. JANGAN kirim ulang transkrip.
            3. Buatlah kesimpulan singkat lalu rangkum ke poin-poin penting (Bullet Points) dalam Bahasa Indonesia yang jelas dan padat.
            4. Gunakan format Markdown.
        """.trimIndent()
    }
}