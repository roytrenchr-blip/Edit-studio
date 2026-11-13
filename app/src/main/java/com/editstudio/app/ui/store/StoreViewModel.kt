package com.editstudio.app.ui.store

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editstudio.app.data.settings.SettingsStore
import com.editstudio.app.store.DownloadManager
import com.editstudio.app.store.DownloadStatus
import com.editstudio.app.store.ManifestRepository
import com.editstudio.app.store.StoreItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StoreUiState(
    val items: List<StoreItem> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isOffline: Boolean = false,
    val isWifiOnly: Boolean = false,
    val errorMessage: String? = null
)

class StoreViewModel(private val context: Context) : ViewModel() {

    private val manifestRepo = ManifestRepository(context)
    val downloadManager = DownloadManager(context)
    private val settingsStore = SettingsStore(context)

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    val downloadStates: StateFlow<Map<String, DownloadStatus>> = downloadManager.downloadStates

    val categories = listOf(
        "All",
        "Motion Packs",
        "Effect Packs",
        "Templates",
        "Fonts",
        "Stickers and Shapes",
        "LUT Looks",
        "Music",
        "Sound Effects",
        "Transition Packs",
        "Overlays"
    )

    init {
        loadManifest()
        observeSettings()
    }

    fun loadManifest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            settingsStore.contentSourceUrl.collect { url ->
                val isOnline = downloadManager.isNetworkAvailable()
                val items = manifestRepo.getManifest(url)
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false,
                    isOffline = !isOnline
                )
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.isWifiOnly.collect { wifiOnly ->
                _uiState.value = _uiState.value.copy(isWifiOnly = wifiOnly)
            }
        }
    }

    fun selectCategory(cat: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = cat)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun downloadItem(item: StoreItem) {
        viewModelScope.launch {
            downloadManager.downloadItem(item, _uiState.value.isWifiOnly)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
