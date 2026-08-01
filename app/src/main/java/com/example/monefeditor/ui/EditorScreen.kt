package com.example.monefeditor.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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

    val themeColor = if (state.settings.darkTheme) MaterialTheme.colorScheme.background else androidx.compose.ui.graphics.Color.White
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.loadFileFromUri(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColor)
            .padding(12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Monef Editor • Stage 3",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.createNewTab() }) { Text("New tab") }
            Button(onClick = { viewModel.saveSession() }) { Text("Save session") }
            Button(onClick = { viewModel.restoreSession() }) { Text("Restore") }
            Button(onClick = { viewModel.refreshSavedFiles() }) { Text("Refresh") }
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = state.settings.darkTheme, onCheckedChange = { viewModel.toggleDarkTheme() })
            Text("Dark theme")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Font size")
            Slider(
                value = state.settings.fontSize,
                onValueChange = { viewModel.updateFontSize(it) },
                valueRange = 12f..24f,
                modifier = Modifier.padding(start = 8.dp)
            )
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
            Button(onClick = { viewModel.deleteCurrentFile(fileName.value) }) { Text("Delete") }
            Button(onClick = { filePickerLauncher.launch(arrayOf("text/plain", "application/json", "text/markdown", "application/xml", "application/octet-stream")) }) { Text("Pick") }
        }

        if (state.savedFiles.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Saved files", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    state.savedFiles.forEach { savedFile ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(savedFile, modifier = Modifier.weight(1f))
                            Button(onClick = {
                                fileName.value = savedFile
                                viewModel.loadFile(savedFile)
                            }) { Text("Open") }
                        }
                    }
                }
            }
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
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Lines: ${activeTab.content.lines().size} • Words: ${activeTab.content.split(Regex("\\s+"))
                        .filter { it.isNotBlank() }.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = activeTab.content,
                    onValueChange = { viewModel.updateActiveTabContent(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = androidx.compose.ui.unit.TextUnit(state.settings.fontSize, androidx.compose.ui.unit.TextUnitType.Sp)
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = themeColor,
                        unfocusedContainerColor = themeColor,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    ),
                    minLines = 20,
                    singleLine = false
                )
            }
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
