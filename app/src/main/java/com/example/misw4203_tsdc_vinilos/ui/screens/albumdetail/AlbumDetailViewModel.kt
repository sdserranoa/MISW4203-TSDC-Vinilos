package com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Carga el detalle del álbum identificado por [albumId]. */
class AlbumDetailViewModel(
    private val albumId: Int,
    private val repository: AlbumRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        loadAlbum()
    }

    fun loadAlbum() {
        _uiState.value = AlbumDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val album = repository.getAlbum(albumId)
                AlbumDetailUiState.Success(album)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AlbumDetailUiState.Error
            }
        }
    }

    companion object {
        fun provideFactory(
            albumId: Int,
            repository: AlbumRepository = AlbumRepository(),
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AlbumDetailViewModel(
                    albumId = albumId,
                    repository = repository,
                )
            }
        }
    }
}
