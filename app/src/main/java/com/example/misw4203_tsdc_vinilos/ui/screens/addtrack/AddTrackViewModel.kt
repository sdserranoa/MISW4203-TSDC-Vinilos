package com.example.misw4203_tsdc_vinilos.ui.screens.addtrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel para la pantalla de asociación de canción (HU08). */
class AddTrackViewModel(
    private val albumId: Int,
    private val repository: AlbumRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTrackUiState>(AddTrackUiState.Loading)
    val uiState: StateFlow<AddTrackUiState> = _uiState.asStateFlow()

    init {
        loadAlbumHeader()
    }

    fun loadAlbumHeader() {
        _uiState.value = AddTrackUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val album = repository.getAlbum(albumId)
                AddTrackUiState.Form(album = album)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AddTrackUiState.ErrorLoadingAlbum("Ocurrió un error al cargar el álbum.")
            }
        }
    }

    fun onNameChange(name: String) {
        val current = _uiState.value as? AddTrackUiState.Form ?: return
        if (name.length > 60) return
        _uiState.value = current.copy(
            name = name,
            errorMessage = null,
        )
    }

    fun onDurationChange(duration: String) {
        val current = _uiState.value as? AddTrackUiState.Form ?: return
        _uiState.value = current.copy(
            duration = duration,
            errorMessage = null,
        )
    }

    fun onSuggestionSelected(suggestion: String) {
        onDurationChange(suggestion)
    }

    fun submitTrack(
        onSuccessNavBack: () -> Unit,
        onErrorNavBack: () -> Unit,
    ) {
        val current = _uiState.value as? AddTrackUiState.Form ?: return
        if (!current.isValid) return

        _uiState.value = current.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                repository.addTrackToAlbum(
                    albumId = albumId,
                    name = current.name.trim(),
                    duration = current.duration.trim(),
                )
                _uiState.value = current.copy(isSubmitting = false, isSuccess = true)
                onSuccessNavBack()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = current.copy(
                    isSubmitting = false,
                    errorMessage = "No se pudo asociar la canción al álbum. Por favor, reintenta.",
                )
                onErrorNavBack()
            }
        }
    }

    companion object {
        fun provideFactory(
            albumId: Int,
            repository: AlbumRepository = AlbumRepository(),
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddTrackViewModel(
                    albumId = albumId,
                    repository = repository,
                )
            }
        }
    }
}
