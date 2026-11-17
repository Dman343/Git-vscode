package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.SettingsDocument
import com.example.data.WorkspaceSettingEntry
import com.example.data.WorkspaceSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ConfigTab {
    SETTINGS,
    RAW_JSON
}

data class WorkspaceUiState(
    val entries: List<WorkspaceSettingEntry> = emptyList(),
    val formattedJson: String = "",
    val isModifiedFromRepo: Boolean = false,
    val selectedTab: ConfigTab = ConfigTab.SETTINGS,
    val isEditingRawJson: Boolean = false,
    val rawJsonDraft: String = "",
    val rawJsonError: String? = null,
    val isDarkTheme: Boolean = true
)

class WorkspaceSettingsViewModel(
    private val repository: WorkspaceSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    init {
        applyDocument(repository.loadSettings())
    }

    fun selectTab(tab: ConfigTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun toggleSetting(key: String, newValue: Boolean) {
        val doc = repository.updateBooleanSetting(key, newValue)
        applyDocument(doc)
    }

    fun resetKeyToDefault(key: String) {
        val doc = repository.resetKeyToDefault(key)
        applyDocument(doc)
    }

    fun resetAllToRepoDefaults() {
        val doc = repository.resetToRepoDefaults()
        applyDocument(doc)
    }

    fun startEditingRawJson() {
        _uiState.update { state ->
            state.copy(
                isEditingRawJson = true,
                rawJsonDraft = state.formattedJson,
                rawJsonError = null
            )
        }
    }

    fun updateRawJsonDraft(draft: String) {
        _uiState.update { it.copy(rawJsonDraft = draft, rawJsonError = null) }
    }

    fun cancelEditingRawJson() {
        _uiState.update { state ->
            state.copy(
                isEditingRawJson = false,
                rawJsonDraft = state.formattedJson,
                rawJsonError = null
            )
        }
    }

    fun applyRawJsonDraft(): Boolean {
        val draft = _uiState.value.rawJsonDraft
        val result = repository.applyRawJson(draft)
        return result.fold(
            onSuccess = { doc ->
                applyDocument(doc)
                true
            },
            onFailure = { error ->
                _uiState.update { it.copy(rawJsonError = error.message ?: "Invalid JSON") }
                false
            }
        )
    }

    private fun applyDocument(doc: SettingsDocument) {
        _uiState.update { state ->
            state.copy(
                entries = doc.entries,
                formattedJson = doc.formattedJson,
                isModifiedFromRepo = doc.isModifiedFromRepo,
                isEditingRawJson = false,
                rawJsonDraft = doc.formattedJson,
                rawJsonError = null
            )
        }
    }

    class Factory(
        private val repository: WorkspaceSettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkspaceSettingsViewModel(repository) as T
        }
    }
}
