package com.example.monefeditor.data

import android.content.Context
import com.example.monefeditor.domain.TextFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPreferencesTextFileRepository(
    private val context: Context
) : TextFileRepository {
    private val prefs = context.getSharedPreferences("monef_editor_files", Context.MODE_PRIVATE)

    override suspend fun saveFile(name: String, content: String): Boolean = withContext(Dispatchers.IO) {
        prefs.edit().putString(name, content).commit()
    }

    override suspend fun loadFile(name: String): String? = withContext(Dispatchers.IO) {
        prefs.getString(name, null)
    }

    override suspend fun listFiles(): List<String> = withContext(Dispatchers.IO) {
        prefs.all.keys.toList().sorted()
    }

    override suspend fun deleteFile(name: String): Boolean = withContext(Dispatchers.IO) {
        prefs.edit().remove(name).commit()
    }
}
