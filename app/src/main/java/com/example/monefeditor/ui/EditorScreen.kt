package com.example.monefeditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EditorScreen(modifier: Modifier = Modifier, viewModel: EditorViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val activeTab = state.tabs.firstOrNull { it.id == state.activeTabId } ?: state.tabs.firstOrNull()
    val fileName = remember { mutableStateOf("note.txt") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Monef Editor • Stage 2",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.createNewTab() }) { Text("New tab") }
            Button(onClick = { viewModel.saveSession() }) { Text("Save session") }
            Button(onClick = { viewModel.restoreSession() }) { Text("Restore") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            state.tabs.forEach { tab ->
                Button(onClick = { viewModel.selectTab(tab.id) }) { Text(tab.title) }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = state.autoIndent, onCheckedChange = { viewModel.toggleAutoIndent() })
            Text("Auto-indent")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = state.useRegex, onCheckedChange = { viewModel.toggleRegex() })
            Text("Regex")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            Button(onClick = { viewModel.findMatches() }) { Text("Find") }
            Button(onClick = { viewModel.replaceAllMatches() }) { Text("Replace") }
            Button(onClick = { viewModel.applyAutoIndentToActiveTab() }) { Text("Indent") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            OutlinedTextField(
                value = fileName.value,
                onValueChange = { fileName.value = it },
                modifier = Modifier.weight(1f),
                label = { Text("File name") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            )
            Button(onClick = { viewModel.saveCurrentFile(fileName.value) }) { Text("Save") }
            Button(onClick = { viewModel.loadFile(fileName.value) }) { Text("Open") }
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            label = { Text("Search") },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
        )

        OutlinedTextField(
            value = state.replaceQuery,
            onValueChange = viewModel::onReplaceQueryChanged,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            label = { Text("Replace") },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
        )

        if (activeTab != null) {
            OutlinedTextField(
                value = activeTab.content,
                onValueChange = { viewModel.updateActiveTabContent(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                ),
                minLines = 20
            )
        }

        if (state.statusMessage.isNotBlank()) {
            Text(
                text = state.statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
