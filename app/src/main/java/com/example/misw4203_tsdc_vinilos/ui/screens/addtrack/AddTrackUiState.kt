package com.example.misw4203_tsdc_vinilos.ui.screens.addtrack

import com.example.misw4203_tsdc_vinilos.model.Album

/** Estado de UI para la pantalla de asociación de canción (HU08). */
sealed interface AddTrackUiState {
    data object Loading : AddTrackUiState
    data class ErrorLoadingAlbum(val message: String) : AddTrackUiState

    data class Form(
        val album: Album,
        val name: String = "",
        val duration: String = "",
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false,
    ) : AddTrackUiState {
        val isNameValid: Boolean
            get() = name.trim().isNotBlank() && name.length <= 60

        val isDurationValid: Boolean
            get() {
                val trimmed = duration.trim()
                if (trimmed.isBlank()) return false
                val parts = trimmed.split(":")
                if (parts.size != 2) return false
                val min = parts[0].toIntOrNull()
                val sec = parts[1].toIntOrNull()
                return min != null && sec != null && min >= 0 && sec in 0..59
            }

        val isValid: Boolean
            get() = isNameValid && isDurationValid && !isSubmitting
    }
}
