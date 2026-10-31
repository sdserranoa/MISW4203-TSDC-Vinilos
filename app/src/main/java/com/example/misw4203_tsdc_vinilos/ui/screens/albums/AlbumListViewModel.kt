package com.example.misw4203_tsdc_vinilos.ui.screens.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.model.Album
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Carga el catálogo completo de álbumes y lo pagina en el cliente, ya que `GET /albums`
 * devuelve todos los registros en una sola respuesta.
 */
class AlbumListViewModel(
    private val repository: AlbumRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumListUiState>(AlbumListUiState.Loading)
    val uiState: StateFlow<AlbumListUiState> = _uiState.asStateFlow()

    private var allAlbums: List<Album> = emptyList()
    private var searchQuery: String = ""

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        _uiState.value = AlbumListUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                allAlbums = repository.getAlbums()
                pageState(page = 1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AlbumListUiState.Error
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        if (searchQuery == query) return
        searchQuery = query
        if (allAlbums.isNotEmpty()) {
            _uiState.value = pageState(page = 1)
        }
    }

    fun nextPage() {
        val state = _uiState.value as? AlbumListUiState.Success ?: return
        if (state.hasNextPage) _uiState.value = pageState(state.currentPage + 1)
    }

    fun previousPage() {
        val state = _uiState.value as? AlbumListUiState.Success ?: return
        if (state.hasPreviousPage) _uiState.value = pageState(state.currentPage - 1)
    }

    private fun filteredAlbums(): List<Album> {
        if (searchQuery.isBlank()) return allAlbums
        val query = searchQuery.trim()
        return allAlbums.filter { album ->
            album.name.contains(query, ignoreCase = true) ||
                    album.genre.contains(query, ignoreCase = true) ||
                    album.performers.any { it.name.contains(query, ignoreCase = true) }
        }
    }

    private fun pageState(page: Int): AlbumListUiState {
        if (allAlbums.isEmpty()) return AlbumListUiState.Empty
        val filtered = filteredAlbums()
        val totalPages = if (filtered.isEmpty()) 1 else (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE
        val safePage = page.coerceIn(1, totalPages)
        return AlbumListUiState.Success(
            albums = filtered.drop((safePage - 1) * PAGE_SIZE).take(PAGE_SIZE),
            totalAlbums = filtered.size,
            currentPage = safePage,
            totalPages = totalPages,
            searchQuery = searchQuery,
        )
    }

    companion object {
        const val PAGE_SIZE = 10

        val Factory = viewModelFactory {
            initializer { AlbumListViewModel(AlbumRepository()) }
        }
    }
}
