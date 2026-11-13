package com.example.misw4203_tsdc_vinilos.ui.screens.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.model.Album
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * Carga el catálogo completo de álbumes y lo pagina en el cliente, ya que `GET /albums`
 * devuelve todos los registros en una sola respuesta (HU01).
 *
 * El filtrado por texto y el cálculo de la página se ejecutan en [defaultDispatcher]; si llega
 * una nueva búsqueda o un cambio de página antes de terminar, el cálculo anterior se cancela.
 */
class AlbumListViewModel(
    private val repository: AlbumRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlbumListUiState>(AlbumListUiState.Loading)
    val uiState: StateFlow<AlbumListUiState> = _uiState.asStateFlow()

    private var allAlbums: List<Album> = emptyList()
    private var searchQuery: String = ""
    private var pageJob: Job? = null

    init {
        loadAlbums()
    }

    fun loadAlbums() {
        pageJob?.cancel()
        _uiState.value = AlbumListUiState.Loading
        pageJob = viewModelScope.launch {
            _uiState.value = try {
                allAlbums = repository.getAlbums()
                computePage(page = 1)
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
        val current = _uiState.value as? AlbumListUiState.Success ?: return
        // El campo de búsqueda refleja de inmediato lo escrito; el filtrado se calcula aparte.
        _uiState.value = current.copy(searchQuery = query)
        showPage(page = 1)
    }

    fun nextPage() {
        val state = _uiState.value as? AlbumListUiState.Success ?: return
        if (state.hasNextPage) showPage(state.currentPage + 1)
    }

    fun previousPage() {
        val state = _uiState.value as? AlbumListUiState.Success ?: return
        if (state.hasPreviousPage) showPage(state.currentPage - 1)
    }

    private fun showPage(page: Int) {
        pageJob?.cancel()
        pageJob = viewModelScope.launch {
            _uiState.value = computePage(page)
        }
    }

    private suspend fun computePage(page: Int): AlbumListUiState {
        val albums = allAlbums
        val query = searchQuery
        return withContext(defaultDispatcher) { pageOf(albums, query, page) }
    }

    companion object {
        const val PAGE_SIZE = 10

        val Factory = viewModelFactory {
            initializer { AlbumListViewModel(AlbumRepository()) }
        }

        /**
         * Filtra [albums] por nombre, género o intérprete y devuelve la página [page] (desde 1).
         * La página se expone como una vista de la lista filtrada (`subList`), sin copiar los elementos.
         */
        internal fun pageOf(
            albums: List<Album>,
            query: String,
            page: Int,
        ): AlbumListUiState {
            if (albums.isEmpty()) return AlbumListUiState.Empty
            val term = query.trim()
            val filtered = if (term.isEmpty()) {
                albums
            } else {
                albums.filter { album ->
                    album.name.contains(term, ignoreCase = true) ||
                        album.genre.contains(term, ignoreCase = true) ||
                        album.performers.any { it.name.contains(term, ignoreCase = true) }
                }
            }
            if (filtered.isEmpty()) {
                return AlbumListUiState.Success(
                    albums = emptyList(),
                    totalAlbums = 0,
                    currentPage = 1,
                    totalPages = 1,
                    searchQuery = query,
                )
            }
            val totalPages = (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE
            val safePage = page.coerceIn(1, totalPages)
            val fromIndex = (safePage - 1) * PAGE_SIZE
            return AlbumListUiState.Success(
                albums = filtered.subList(fromIndex, min(fromIndex + PAGE_SIZE, filtered.size)),
                totalAlbums = filtered.size,
                currentPage = safePage,
                totalPages = totalPages,
                searchQuery = query,
            )
        }
    }
}
