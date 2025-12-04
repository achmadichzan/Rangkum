package com.achmadichzan.rangkum.presentation.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

object MarkdownParser {
    fun parse(
        text: String,
        primaryColor: Color,
        onBackground: Color = Color.Black,
        lineHeight: TextUnit = TextUnit.Unspecified
    ): AnnotatedString {
        val codeBackground = onBackground.copy(alpha = 0.1f)

        return buildAnnotatedString {
            val lines = text.split("\n")
            var inCodeBlock = false

            // Hapus baris kosong di AWAL dan AKHIR file saja, jangan di tengah
            val filteredLines = lines.dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }

            // Gunakan forEachIndexed agar kita tahu kapan harus menambah \n
            filteredLines.forEachIndexed { index, line ->
                val trimmedLine = line.trim()

                // 1. JANGAN SKIP BARIS KOSONG. TAPI RENDER SEBAGAI ENTER.
                if (!inCodeBlock && trimmedLine.isBlank()) {
                    if (index < filteredLines.lastIndex) {
                        append("\n")
                    }
                    return@forEachIndexed
                }

                // 2. LOGIKA CODE BLOCK
                if (trimmedLine.startsWith("```")) {
                    inCodeBlock = !inCodeBlock
                    // Jangan append teks ```, tapi pastikan ada enter jika bukan baris terakhir
                    if (index < filteredLines.lastIndex) append("\n")
                    return@forEachIndexed
                }

                if (inCodeBlock) {
                    withStyle(style = ParagraphStyle(lineHeight = lineHeight)) {
                        withStyle(
                            style = SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = codeBackground,
                                fontSize = 13.sp
                            )
                        ) {
                            append(line)
                        }
                    }
                }
                else {
                    // 3. LOGIKA STYLING BIASA
                    if (trimmedLine.startsWith("### ")) {
                        withStyle(style = ParagraphStyle(lineHeight = lineHeight)) {
                            withStyle(style = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                                append(trimmedLine.substring(4))
                            }
                        }
                    }
                    else if (trimmedLine.startsWith("## ")) {
                        withStyle(style = ParagraphStyle(lineHeight = lineHeight)) {
                            withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                                append(trimmedLine.substring(3))
                            }
                        }
                    }
                    else if (trimmedLine.startsWith("# ")) {
                        withStyle(style = ParagraphStyle(lineHeight = lineHeight)) {
                            withStyle(style = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                                append(trimmedLine.substring(2))
                            }
                        }
                    }
                    else if (trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ")) {
                        withStyle(
                            style = ParagraphStyle(
                                textIndent = TextIndent(firstLine = 16.sp, restLine = 16.sp),
                                lineHeight = lineHeight
                            )
                        ) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                                append("•  ")
                            }
                            parseInlineFormatting(trimmedLine.substring(2), primaryColor, codeBackground)
                        }
                    }
                    else if (trimmedLine.matches(Regex("^\\d+\\.\\s.*"))) {
                        val dotIndex = trimmedLine.indexOf(".")
                        val numberPart = trimmedLine.take(dotIndex + 1)
                        val contentPart = trimmedLine.substring(dotIndex + 1).trim()

                        withStyle(style = ParagraphStyle(lineHeight = lineHeight)) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                                append("$numberPart  ")
                            }
                            parseInlineFormatting(contentPart, primaryColor, codeBackground)
                        }
                    }
                    else {
                        withStyle(style = ParagraphStyle(lineHeight = lineHeight)) {
                            parseInlineFormatting(line, primaryColor, codeBackground)
                        }
                    }
                }

                // 4. KUNCI UTAMA: KEMBALIKAN ENTER YANG HILANG KARENA SPLIT
                // Jika ini bukan baris terakhir, tambahkan enter manual.
                if (index < filteredLines.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.parseInlineFormatting(
        text: String,
        primaryColor: Color,
        codeBackground: Color
    ) {
        val combinedRegex = Regex("(\\*\\*(.*?)\\*\\*)|(`(.*?)`)|(\\*(.*?)\\*)")
        var cursor = 0
        val matches = combinedRegex.findAll(text)

        matches.forEach { match ->
            if (match.range.first > cursor) {
                append(text.substring(cursor, match.range.first))
            }

            val value = match.value
            when {
                value.startsWith("**") -> {
                    val content = match.groupValues[2]
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                        append(content)
                    }
                }
                value.startsWith("`") -> {
                    val content = match.groupValues[4]
                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBackground,
                            fontSize = 13.sp
                        )
                    ) {
                        append(content)
                    }
                }
                value.startsWith("*") -> {
                    val content = match.groupValues[6]
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(content)
                    }
                }
            }
            cursor = match.range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}