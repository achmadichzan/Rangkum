package com.achmadichzan.rangkum.presentation.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

object MarkdownParser {
    fun parse(
        text: String,
        primaryColor: Color,
        onBackground: Color = Color.Black
    ): AnnotatedString {

        val codeBackgroundColor = onBackground.copy(alpha = 0.1f)
        val codeFontColor = onBackground.copy(alpha = 0.9f)

        return buildAnnotatedString {
            val lines = text.split("\n")
            var inCodeBlock = false

            lines.forEachIndexed { index, line ->
                val trimmedLine = line.trim()

                if (trimmedLine.startsWith("```")) {
                    inCodeBlock = !inCodeBlock
                    return@forEachIndexed
                }

                if (inCodeBlock) {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = codeBackgroundColor,
                            color = codeFontColor,
                            fontSize = 13.sp
                        )
                    ) {
                        append(line)
                    }
                }
                else {
                    processLine(line, trimmedLine, primaryColor, codeBackgroundColor)
                }

                if (index < lines.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.processLine(
        originalLine: String,
        trimmedLine: String,
        primaryColor: Color,
        codeBackgroundColor: Color
    ) {
        when {
            trimmedLine.startsWith("#") -> {
                val level = trimmedLine.takeWhile { it == '#' }.length
                val content = trimmedLine.substring(level).trimStart()

                val fontSize = when (level) {
                    1 -> 20.sp
                    2 -> 18.sp
                    3 -> 17.sp
                    else -> 16.sp
                }

                withStyle(style = SpanStyle(fontSize = fontSize, fontWeight = FontWeight.Bold)) {
                    append(content)
                }
            }

            (trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ")) -> {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                    append("• ")
                }
                parseInlineFormatting(trimmedLine.substring(2), primaryColor, codeBackgroundColor)
            }

            trimmedLine.matches(Regex("^\\d+\\.\\s.*")) -> {
                val dotIndex = trimmedLine.indexOf(".")
                val numberPart = trimmedLine.take(dotIndex + 1)
                val contentPart = trimmedLine.substring(dotIndex + 1).trimStart()

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                    append("$numberPart ")
                }
                parseInlineFormatting(contentPart, primaryColor, codeBackgroundColor)
            }

            else -> {
                parseInlineFormatting(originalLine, primaryColor, codeBackgroundColor)
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