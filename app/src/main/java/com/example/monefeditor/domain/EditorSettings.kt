package com.example.monefeditor.domain

data class EditorSettings(
    val darkTheme: Boolean = true,
    val fontSize: Float = 16f,
    val tabSize: Int = 4,
    val lineNumbers: Boolean = true,
    val useMonospace: Boolean = true
)
