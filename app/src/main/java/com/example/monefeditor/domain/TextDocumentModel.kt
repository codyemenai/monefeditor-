package com.example.monefeditor.domain

import java.util.Locale

class TextDocumentModel(
    val id: Int,
    val title: String,
    var content: String,
    var language: String = "text"
) {
    fun lineCount(): Int = content.lines().size

    fun wordCount(): Int = content.split(Regex("\\s+")).filter { it.isNotBlank() }.size

    fun getPreview(maxChars: Int = 80): String = content.take(maxChars).replace("\n", " ")

    fun detectLanguageFromName(): String = when (title.lowercase(Locale.getDefault())) {
        "kotlin", "kt", "kts" -> "kotlin"
        "java", "jav" -> "java"
        "json" -> "json"
        "md", "markdown" -> "markdown"
        "xml", "html", "htm" -> "xml"
        else -> "text"
    }
}
