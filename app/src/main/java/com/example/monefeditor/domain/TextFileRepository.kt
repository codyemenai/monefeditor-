package com.example.monefeditor.domain

interface TextFileRepository {
    suspend fun saveFile(name: String, content: String): Boolean
    suspend fun loadFile(name: String): String?
    suspend fun listFiles(): List<String>
    suspend fun deleteFile(name: String): Boolean
}
