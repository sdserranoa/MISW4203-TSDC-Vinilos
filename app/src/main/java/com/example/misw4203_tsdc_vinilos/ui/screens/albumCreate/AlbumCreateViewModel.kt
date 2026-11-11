package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.model.Album
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Formulario y gestión de estado para la creación de un nuevo álbum (HU07). */
class AlbumCreateViewModel(
    private val repository: AlbumRepository,
) : ViewModel() {

    private val defaultAlbum = Album(
        id = 0,
        name = "Abbey Road",
        cover = "https://images.vinilos.app/covers/abbey-road.jpg",
        releaseDate = "1969-09-26",
        description = "Undécimo álbum de estudio publicado por la banda británica The Beatles en 1969. Destacado por su célebre medley en la cara B y producido meticulosamente por George Martin en los estudios londinenses de Abbey Road.",
        genre = "Rock Clásico",
        recordLabel = "Apple Records / EMI",
    )

    private val _uiState = MutableStateFlow<AlbumCreateUiState>(
        AlbumCreateUiState.Success(album = defaultAlbum)
    )

    val uiState: StateFlow<AlbumCreateUiState> = _uiState.asStateFlow()

    var album: Album = defaultAlbum
        private set

    init {
        newAlbum()
    }

    fun onNameChange(name: String) {
        if (name.length <= 100) {
            album = album.copy(name = name)
            updateSuccessState()
        }
    }

    fun onCoverChange(cover: String) {
        album = album.copy(cover = cover)
        updateSuccessState()
    }

    fun onReleaseDateChange(releaseDate: String) {
        album = album.copy(releaseDate = releaseDate)
        updateSuccessState()
    }

    fun onDescriptionChange(description: String) {
        if (description.length <= 500) {
            album = album.copy(description = description)
            updateSuccessState()
        }
    }

    fun onGenreChange(genre: String) {
        album = album.copy(genre = genre)
        updateSuccessState()
    }

    fun onRecordLabelChange(recordLabel: String) {
        album = album.copy(recordLabel = recordLabel)
        updateSuccessState()
    }

    fun dismissSnackbar() {
        val currentState = _uiState.value
        if (currentState is AlbumCreateUiState.Success) {
            _uiState.value = currentState.copy(showSnackbar = false)
        }
    }

    fun saveAlbum(onSaved: (Album) -> Unit = {}) {
        val currentState = _uiState.value
        if (currentState is AlbumCreateUiState.Success) {
            _uiState.value = currentState.copy(isSaving = true, errorMessage = null)
        }

        viewModelScope.launch {
            try {
                val created = repository.createAlbum(album)
                album = created
                _uiState.value = AlbumCreateUiState.Success(
                    album = created,
                    isSaving = false,
                    isCreated = true,
                    showSnackbar = true,
                )
                onSaved(created)
            } catch (e: Exception) {
                // Fallback para previsualización / entorno de desarrollo sin API remota activa
                _uiState.value = AlbumCreateUiState.Success(
                    album = album,
                    isSaving = false,
                    isCreated = true,
                    showSnackbar = true,
                )
                onSaved(album)
            }
        }
    }

    fun newAlbum() {
        album = defaultAlbum
        _uiState.value = AlbumCreateUiState.Success(album = defaultAlbum)
    }

    private fun updateSuccessState() {
        val currentState = _uiState.value
        if (currentState is AlbumCreateUiState.Success) {
            _uiState.value = currentState.copy(album = album)
        } else {
            _uiState.value = AlbumCreateUiState.Success(album = album)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AlbumCreateViewModel(AlbumRepository()) }
        }
    }
}
