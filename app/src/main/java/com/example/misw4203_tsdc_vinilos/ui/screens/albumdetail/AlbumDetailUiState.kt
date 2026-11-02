package com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail

import com.example.misw4203_tsdc_vinilos.model.Album

/** Estados posibles de la pantalla de detalle de un álbum (HU02). */
sealed interface AlbumDetailUiState {

    data object Loading : AlbumDetailUiState

    data object Error : AlbumDetailUiState

    data class Success(val album: Album) : AlbumDetailUiState
}
