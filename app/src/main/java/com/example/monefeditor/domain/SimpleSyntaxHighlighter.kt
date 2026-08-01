package com.example.monefeditor.domain

import androidx.compose.ui.graphics.Color

class SimpleSyntaxHighlighter {
    fun highlight(text: String, language: String): List<Pair<String, Color>> {
        val tokens = mutableListOf<Pair<String, Color>>() 
        val safeLanguage = language.lowercase()

        when (safeLanguage) {
            "kotlin" -> {
                text.split(Regex("(\\s+|[{}()\[\];,.])")).forEach { token ->
                    val trimmed = token.trim()
                    if (trimmed.isEmpty()) return@forEach
                    val color = when {
                        trimmed in setOf("fun", "class", "object", "val", "var", "if", "else", "for", "while", "return", "import") -> Color(0xFF7C4DFF)
                        trimmed.startsWith("\"") || trimmed.endsWith("\"") -> Color(0xFF4CAF50)
                        trimmed.startsWith("//") -> Color(0xFF757575)
                        else -> Color(0xFF212121)
                    }
                    tokens.add(trimmed to color)
                }
            }
            "json" -> {
                text.split(Regex("(\\s+|[{}\[\]:,])")).forEach { token ->
                    val trimmed = token.trim()
                    if (trimmed.isEmpty()) return@forEach
                    val color = when {
                        trimmed.startsWith("\"") -> Color(0xFF4CAF50)
                        trimmed in setOf("true", "false", "null") -> Color(0xFF7C4DFF)
                        else -> Color(0xFF212121)
                    }
                    tokens.add(trimmed to color)
                }
            }
            else -> {
                tokens.add(text to Color(0xFF212121))
            }
        }

        return tokens
    }
}
