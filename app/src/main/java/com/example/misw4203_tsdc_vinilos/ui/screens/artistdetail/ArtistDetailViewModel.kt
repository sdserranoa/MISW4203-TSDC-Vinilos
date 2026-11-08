package com.example.misw4203_tsdc_vinilos.ui.screens.artistdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.ArtistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Carga el detalle del artista identificado por [artistId] (HU04). */
class ArtistDetailViewModel(
    private val artistId: Int,
    private val isBand: Boolean? = null,
    private val repository: ArtistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArtistDetailUiState>(ArtistDetailUiState.Loading)
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        loadArtist()
    }

    fun loadArtist() {
        _uiState.value = ArtistDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val artist = repository.getArtistDetail(artistId, isBand)
                ArtistDetailUiState.Success(artist = artist, isFavorite = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ArtistDetailUiState.Error
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value as? ArtistDetailUiState.Success ?: return
        _uiState.value = current.copy(isFavorite = !current.isFavorite)
    }

    companion object {
        fun provideFactory(
            artistId: Int,
            isBand: Boolean? = null,
            repository: ArtistRepository = ArtistRepository(),
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ArtistDetailViewModel(
                    artistId = artistId,
                    isBand = isBand,
                    repository = repository,
                )
            }
        }
    }
}
