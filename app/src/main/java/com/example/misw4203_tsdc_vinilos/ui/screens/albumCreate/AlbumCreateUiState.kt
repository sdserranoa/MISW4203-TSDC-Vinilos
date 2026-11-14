package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import com.example.misw4203_tsdc_vinilos.model.Album

enum class AlbumCreateField {
    NAME,
    COVER,
    RELEASE_DATE,
    GENRE,
    RECORD_LABEL,
    DESCRIPTION,
}

enum class AlbumCreateValidationError {
    REQUIRED,
    INVALID_TEXT,
    NAME_TOO_LONG,
    INVALID_URL,
    INVALID_DATE,
    INVALID_OPTION,
}

/** Estados posibles de la pantalla de creación de un álbum (HU07). */
sealed interface AlbumCreateUiState {

    data object Loading : AlbumCreateUiState

    data object Error : AlbumCreateUiState

    data object SaveError : AlbumCreateUiState

    data class Success(
        val album: Album,
        val isSaving: Boolean = false,
        val isCreated: Boolean = false,
        val showSnackbar: Boolean = false,
        val errorMessage: String? = null,
        val validationErrors: Map<AlbumCreateField, AlbumCreateValidationError> = emptyMap(),
    ) : AlbumCreateUiState
}
