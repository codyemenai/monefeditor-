package com.example.monefeditor.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monefeditor.data.SharedPreferencesTextFileRepository
import com.example.monefeditor.domain.TextFileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.max

private const val SESSION_PREFS = "monef_editor_session"
private const val SESSION_KEY = "tabs"
private const val ACTIVE_TAB_KEY = "active_tab"

data class EditorTab(
    val id: Int,
    val title: String,
    val content: String
)

data class EditorUiState(
    val tabs: List<EditorTab> = listOf(
        EditorTab(
            id = 1,
            title = "untitled-1",
            content = "fun main() {\n    println(\"Hello from Monef Editor\")\n}\n"
        )
    ),
    val activeTabId: Int = 1,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val useRegex: Boolean = false,
    val autoIndent: Boolean = true,
    val indentSize: Int = 4,
    val statusMessage: String = ""
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
    private val repository: TextFileRepository = SharedPreferencesTextFileRepository(application)

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    fun createNewTab() {
        val nextId = (_state.value.tabs.maxOfOrNull { it.id } ?: 0) + 1
        val newTab = EditorTab(id = nextId, title = "untitled-$nextId", content = "")
        _state.update { current ->
            current.copy(
                tabs = current.tabs + newTab,
                activeTabId = newTab.id,
                statusMessage = "Created ${newTab.title}"
            )
        }
    }

    fun selectTab(tabId: Int) {
        _state.update { it.copy(activeTabId = tabId) }
    }

    fun updateActiveTabContent(content: String) {
        val activeId = _state.value.activeTabId
        _state.update { current ->
            val updatedTabs = current.tabs.map { tab ->
                if (tab.id == activeId) tab.copy(content = content) else tab
            }
            current.copy(tabs = updatedTabs)
        }
    }

    fun updateActiveTabTitle(title: String) {
        val activeId = _state.value.activeTabId
        _state.update { current ->
            val updatedTabs = current.tabs.map { tab ->
                if (tab.id == activeId) tab.copy(title = title.ifBlank { "untitled" }) else tab
            }
            current.copy(tabs = updatedTabs)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onReplaceQueryChanged(query: String) {
        _state.update { it.copy(replaceQuery = query) }
    }

    fun toggleRegex() {
        _state.update { it.copy(useRegex = !it.useRegex) }
    }

    fun toggleAutoIndent() {
        _state.update { it.copy(autoIndent = !it.autoIndent) }
    }

    fun findMatches(): Int {
        val query = _state.value.searchQuery
        if (query.isBlank()) {
            _state.update { it.copy(statusMessage = "Search is empty") }
            return 0
        }

        val pattern = if (_state.value.useRegex) Regex(query) else Regex(Regex.escape(query))
        val count = _state.value.tabs.sumOf { tab ->
            pattern.findAll(tab.content).count()
        }
        _state.update { it.copy(statusMessage = "Found $count matches") }
        return count
    }

    fun replaceAllMatches() {
        val query = _state.value.searchQuery
        if (query.isBlank()) {
            _state.update { it.copy(statusMessage = "Search is empty") }
            return
        }

        val pattern = if (_state.value.useRegex) Regex(query) else Regex(Regex.escape(query))
        val updatedTabs = _state.value.tabs.map { tab ->
            val newContent = if (query.isBlank()) tab.content else tab.content.replace(pattern, _state.value.replaceQuery)
            tab.copy(content = newContent)
        }

        _state.update {
            it.copy(
                tabs = updatedTabs,
                statusMessage = "Replaced matches in ${updatedTabs.size} tabs"
            )
        }
    }

    fun applyAutoIndentToActiveTab() {
        if (!_state.value.autoIndent) {
            _state.update { it.copy(statusMessage = "Auto-indent is disabled") }
            return
        }

        val activeId = _state.value.activeTabId
        val updatedTabs = _state.value.tabs.map { tab ->
            if (tab.id == activeId) tab.copy(content = autoIndent(tab.content)) else tab
        }

        _state.update {
            it.copy(
                tabs = updatedTabs,
                statusMessage = "Auto-indent applied"
            )
        }
    }

    fun saveSession() {
        val stateToSave = _state.value
        val serialized = stateToSave.tabs.joinToString("::") { tab ->
            encode(tab.title) + "||" + encode(tab.content)
        }
        prefs.edit()
            .putString(SESSION_KEY, serialized)
            .putInt(ACTIVE_TAB_KEY, stateToSave.activeTabId)
            .apply()
        _state.update { it.copy(statusMessage = "Session saved") }
    }

    fun saveCurrentFile(name: String) {
        val activeTab = _state.value.tabs.firstOrNull { it.id == _state.value.activeTabId } ?: return
        viewModelScope.launch {
            val success = repository.saveFile(name, activeTab.content)
            _state.update {
                it.copy(statusMessage = if (success) "Saved $name" else "Failed to save $name")
            }
        }
    }

    fun loadFile(name: String) {
        viewModelScope.launch {
            val content = repository.loadFile(name)
            if (content != null) {
                val activeId = _state.value.activeTabId
                val updatedTabs = _state.value.tabs.map { tab ->
                    if (tab.id == activeId) tab.copy(content = content, title = name) else tab
                }
                _state.update {
                    it.copy(
                        tabs = updatedTabs,
                        statusMessage = "Loaded $name"
                    )
                }
            } else {
                _state.update { it.copy(statusMessage = "File not found") }
            }
        }
    }

    fun listSavedFiles(): List<String> {
        return _state.value.tabs.map { it.title }
    }

    fun restoreSession() {
        val serialized = prefs.getString(SESSION_KEY, null) ?: return
        val activeTabId = prefs.getInt(ACTIVE_TAB_KEY, 1)
        val tabs = serialized.split("::").filter { it.isNotBlank() }.mapNotNull { item ->
            val parts = item.split("||", limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val title = decode(parts[0])
            val content = decode(parts[1])
            EditorTab(id = (title.hashCode()), title = title, content = content)
        }
        if (tabs.isEmpty()) {
            _state.update { it.copy(statusMessage = "No saved session found") }
            return
        }
        _state.update {
            it.copy(
                tabs = tabs,
                activeTabId = activeTabId.takeIf { id -> tabs.any { tab -> tab.id == id } } ?: tabs.first().id,
                statusMessage = "Session restored"
            )
        }
    }

    private fun autoIndent(text: String): String {
        val lines = text.split('\n')
        val result = mutableListOf<String>()
        var indentLevel = 0
        for (line in lines) {
            val trimmed = line.trim()
            val currentIndent = " ".repeat(max(0, indentLevel * _state.value.indentSize))
            when {
                trimmed.startsWith("}") -> {
                    indentLevel = max(0, indentLevel - 1)
                    result.add(" ".repeat(max(0, indentLevel * _state.value.indentSize)) + trimmed)
                }
                trimmed.endsWith("{") || trimmed.endsWith(":") -> {
                    result.add(currentIndent + trimmed)
                    indentLevel += 1
                }
                else -> result.add(currentIndent + trimmed)
            }
        }
        return result.joinToString("\n")
    }

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

    private fun decode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)
}
