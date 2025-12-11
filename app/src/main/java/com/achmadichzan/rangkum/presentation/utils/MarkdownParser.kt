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
        return buildAnnotatedString {
            val lines = text.split("\n")
            var inCodeBlock = false

            val codeBg = onBackground.copy(alpha = 0.1f)

            lines.forEachIndexed { index, line ->
                val trimmedLine = line.trim()

                if (trimmedLine.startsWith("```")) {
                    inCodeBlock = !inCodeBlock
                }

                if (inCodeBlock && !trimmedLine.startsWith("```")) {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBg,
                        fontSize = 13.sp
                    )) {
                        append(line)
                    }
                } else {
                    appendFormattedString(line, primaryColor, codeBg)
                }

                if (index < lines.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    fun parseToBlocks(
        text: String,
        primaryColor: Color,
        onBackground: Color = Color.Black
    ): List<MarkdownBlock> {

        val blocks = mutableListOf<MarkdownBlock>()
        val lines = text.split("\n")
        var inCodeBlock = false
        val codeBuffer = StringBuilder()
        val codeBg = onBackground.copy(alpha = 0.1f)

        fun parseInline(input: String): AnnotatedString {
            return buildAnnotatedString {
                appendFormattedString(input, primaryColor, codeBg)
            }
        }

        lines.forEach { line ->
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock
                if (!inCodeBlock && codeBuffer.isNotEmpty()) {
                    blocks.add(MarkdownBlock.CodeBlock(codeBuffer.toString()))
                    codeBuffer.clear()
                }
                return@forEach
            }

            if (inCodeBlock) {
                codeBuffer.append(line).append("\n")
                return@forEach
            }
            if (trimmed.startsWith("#")) {
                val level = trimmed.takeWhile { it == '#' }.length
                val content = trimmed.substring(level).trimStart()
                blocks.add(MarkdownBlock.Header(AnnotatedString(content), level))
            }
            else if (trimmed.startsWith("* ") || trimmed.startsWith("- ")) {
                val content = trimmed.substring(2).trimStart()

                blocks.add(MarkdownBlock.ListBullet(parseInline(content)))
            }
            else if (trimmed.matches(Regex("^\\d+\\.\\s.*"))) {
                val dotIndex = trimmed.indexOf(".")
                val number = trimmed.take(dotIndex + 1)

                val content = trimmed.substring(dotIndex + 1).trimStart()

                blocks.add(MarkdownBlock.ListNumber(number, parseInline(content)))
            }
            else if (trimmed.startsWith("> ")) {
                val content = trimmed.substring(1).trimStart()
                blocks.add(MarkdownBlock.BlockQuote(parseInline(content)))
            }
            else if (line.isNotEmpty()) {
                blocks.add(MarkdownBlock.Text(parseInline(line)))
            }
        }

        return blocks
    }

    private fun AnnotatedString.Builder.appendFormattedString(
        text: String,
        primaryColor: Color,
        codeBackgroundColor: Color
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
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )) {
                        append(content)
                    }
                }
                value.startsWith("`") -> {
                    val content = match.groupValues[4]
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBackgroundColor,
                        fontSize = 13.sp
                    )) {
                        append(content)
                    }
                }
                value.startsWith("*") -> {
                    val content = match.groupValues[6]
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
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