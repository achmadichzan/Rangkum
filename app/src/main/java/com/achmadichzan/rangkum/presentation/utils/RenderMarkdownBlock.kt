package com.achmadichzan.rangkum.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RenderMarkdownBlock(block: MarkdownBlock, textColor: Color) {
    val baseTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = textColor,
        lineHeight = 20.sp
    )
    val indentWidth = 18.dp

    when (block) {
        is MarkdownBlock.Header -> {
            Text(
                text = block.content,
                color = textColor,
                fontSize = when (block.level) { 1 -> 18.sp; 2 -> 17.sp; else -> 16.sp },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        is MarkdownBlock.CodeBlock -> {
            Text(
                text = block.content,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        textColor.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            )
        }
        is MarkdownBlock.Text -> {
            Text(
                text = block.content,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
        is MarkdownBlock.ListBullet -> {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = baseTextStyle,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(indentWidth),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = block.content,
                    style = baseTextStyle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        is MarkdownBlock.ListNumber -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = block.number,
                    style = baseTextStyle,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(indentWidth),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = block.content,
                    style = baseTextStyle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        is MarkdownBlock.BlockQuote -> {
            Row(modifier = Modifier.fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier.fillMaxHeight()
                        .width(4.dp)
                        .background(
                            textColor.copy(alpha = 0.5f),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = block.content,
                    color = textColor.copy(alpha = 0.8f),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Text(val content: AnnotatedString) : MarkdownBlock()
    data class Header(val content: AnnotatedString, val level: Int) : MarkdownBlock()
    data class CodeBlock(val content: String) : MarkdownBlock()
    data class ListBullet(val content: AnnotatedString) : MarkdownBlock()
    data class ListNumber(val number: String, val content: AnnotatedString) : MarkdownBlock()
    data class BlockQuote(val content: AnnotatedString) : MarkdownBlock()
}