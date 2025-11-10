package com.example.loginandregistration.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * Utility class for rendering markdown text with formatting Supports: # Headers, **bold**,
 * *italic*, `inline code`,
 * ```code
 * ```
 * blocks```, bullet points
 */
object MarkdownRenderer {

    /** Parse markdown text and return formatted SpannableStringBuilder */
    fun parseMarkdown(text: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val lines = text.split("\n")

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Check for headers at start of line
            if (line.trimStart().startsWith("#")) {
                val trimmed = line.trimStart()
                val headerLevel = trimmed.takeWhile { it == '#' }.length
                val headerText = trimmed.substring(headerLevel).trim()

                if (headerText.isNotEmpty()) {
                    appendHeader(builder, headerText, headerLevel)
                    i++
                    continue
                }
            }

            // Check for bullet points (*, -, •)
            val trimmedLine = line.trimStart()
            if (trimmedLine.startsWith("* ") ||
                            trimmedLine.startsWith("- ") ||
                            trimmedLine.startsWith("• ")
            ) {
                val bulletText = trimmedLine.substring(2)
                appendBulletPoint(builder, bulletText)
                i++
                continue
            }

            // Parse inline formatting (bold, italic, code)
            parseInlineFormatting(builder, line)

            // Add newline if not last line
            if (i < lines.size - 1) {
                builder.append("\n")
            }

            i++
        }

        return builder
    }

    /** Parse inline formatting like bold, italic, and inline code */
    private fun parseInlineFormatting(builder: SpannableStringBuilder, text: String) {
        var currentIndex = 0
        val length = text.length

        while (currentIndex < length) {
            when {
                // Bold (**text**)
                text.startsWith("**", currentIndex) -> {
                    val endIndex = text.indexOf("**", currentIndex + 2)
                    if (endIndex != -1 && endIndex > currentIndex + 2) {
                        val boldText = text.substring(currentIndex + 2, endIndex)
                        appendBold(builder, boldText)
                        currentIndex = endIndex + 2
                    } else {
                        builder.append(text[currentIndex])
                        currentIndex++
                    }
                }

                // Italic (*text*) - but not if it's **
                text.startsWith("*", currentIndex) && !text.startsWith("**", currentIndex) -> {
                    val endIndex = text.indexOf("*", currentIndex + 1)
                    if (endIndex != -1 &&
                                    endIndex > currentIndex + 1 &&
                                    !text.startsWith("**", endIndex)
                    ) {
                        val italicText = text.substring(currentIndex + 1, endIndex)
                        appendItalic(builder, italicText)
                        currentIndex = endIndex + 1
                    } else {
                        builder.append(text[currentIndex])
                        currentIndex++
                    }
                }

                // Inline code (`code`)
                text.startsWith("`", currentIndex) && !text.startsWith("```", currentIndex) -> {
                    val endIndex = text.indexOf("`", currentIndex + 1)
                    if (endIndex != -1 && endIndex > currentIndex + 1) {
                        val codeText = text.substring(currentIndex + 1, endIndex)
                        appendInlineCode(builder, codeText)
                        currentIndex = endIndex + 1
                    } else {
                        builder.append(text[currentIndex])
                        currentIndex++
                    }
                }
                else -> {
                    builder.append(text[currentIndex])
                    currentIndex++
                }
            }
        }
    }

    private fun appendBold(builder: SpannableStringBuilder, text: String) {
        val start = builder.length
        builder.append(text)
        builder.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                builder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun appendItalic(builder: SpannableStringBuilder, text: String) {
        val start = builder.length
        builder.append(text)
        builder.setSpan(
                StyleSpan(Typeface.ITALIC),
                start,
                builder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun appendInlineCode(builder: SpannableStringBuilder, text: String) {
        val start = builder.length
        builder.append(text)
        builder.setSpan(
                TypefaceSpan("monospace"),
                start,
                builder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun appendBulletPoint(builder: SpannableStringBuilder, text: String) {
        if (builder.isNotEmpty() && builder[builder.length - 1] != '\n') {
            builder.append("\n")
        }
        builder.append("  • ")
        parseInlineFormatting(builder, text)
    }

    private fun appendHeader(builder: SpannableStringBuilder, text: String, level: Int) {
        if (builder.isNotEmpty() && builder[builder.length - 1] != '\n') {
            builder.append("\n")
        }

        val start = builder.length

        // Parse inline formatting within header
        parseInlineFormatting(builder, text)

        // Apply bold style to entire header
        builder.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                builder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply size based on header level
        val sizeMultiplier =
                when (level) {
                    1 -> 1.8f // # Largest
                    2 -> 1.5f // ## Large
                    3 -> 1.3f // ### Medium
                    4 -> 1.2f // #### Slightly larger
                    5 -> 1.1f // ##### Small
                    else -> 1.0f
                }

        builder.setSpan(
                RelativeSizeSpan(sizeMultiplier),
                start,
                builder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        builder.append("\n")
        if (level <= 2) {
            builder.append("\n") // Extra spacing for main headers
        }
    }

    /** Copy text to clipboard */
    fun copyToClipboard(context: Context, text: String, label: String = "Code") {
        val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    /** Extract code blocks from markdown text */
    fun extractCodeBlocks(text: String): List<CodeBlock> {
        val codeBlocks = mutableListOf<CodeBlock>()
        var currentIndex = 0

        while (currentIndex < text.length) {
            val startIndex = text.indexOf("```", currentIndex)
            if (startIndex == -1) break

            val endIndex = text.indexOf("```", startIndex + 3)
            if (endIndex == -1) break

            val codeContent = text.substring(startIndex + 3, endIndex).trim()
            val lines = codeContent.lines()
            val language =
                    if (lines.isNotEmpty() && lines[0].length < 20 && !lines[0].contains(" ")) {
                        lines[0]
                    } else {
                        ""
                    }

            val code =
                    if (language.isNotEmpty()) {
                        lines.drop(1).joinToString("\n")
                    } else {
                        codeContent
                    }

            codeBlocks.add(CodeBlock(code, language, startIndex, endIndex + 3))
            currentIndex = endIndex + 3
        }

        return codeBlocks
    }

    data class CodeBlock(
            val code: String,
            val language: String,
            val startIndex: Int,
            val endIndex: Int
    )
}
