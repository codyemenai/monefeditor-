package com.example.monefeditor.data

import android.content.Context
import com.example.monefeditor.domain.TextFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets

class InternalStorageTextFileRepository(
    private val context: Context
) : TextFileRepository {
    private val documentsDir: File
        get() = File(context.filesDir, "documents").apply { mkdirs() }

    override suspend fun saveFile(name: String, content: String): Boolean = withContext(Dispatchers.IO) {
        val safeName = sanitizeFileName(name)
        val file = File(documentsDir, safeName)
        file.parentFile?.mkdirs()
        file.writeText(content, StandardCharsets.UTF_8)
        true
    }

    override suspend fun loadFile(name: String): String? = withContext(Dispatchers.IO) {
        val safeName = sanitizeFileName(name)
        val file = File(documentsDir, safeName)
        if (file.exists() && file.isFile) {
            file.readText(StandardCharsets.UTF_8)
        } else {
            null
        }
    }

    override suspend fun listFiles(): List<String> = withContext(Dispatchers.IO) {
        documentsDir.listFiles()
            ?.filter { it.isFile }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }

    override suspend fun deleteFile(name: String): Boolean = withContext(Dispatchers.IO) {
        val safeName = sanitizeFileName(name)
        val file = File(documentsDir, safeName)
        file.exists() && file.isFile && file.delete()
    }

    private fun sanitizeFileName(name: String): String {
        val trimmed = name.trim().ifBlank { "untitled.txt" }
        return trimmed.replace(Regex("[\\\\/:*?\"<>|]+"), "_")
    }
}
