package com.example.misw4203_tsdc_vinilos.ui.screens.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.ArtistRepository
import com.example.misw4203_tsdc_vinilos.model.Performer
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
 * Carga el catálogo consolidado de artistas (músicos y bandas) y lo pagina en el cliente (HU03).
 *
 * El filtrado por texto/tipo de artista y el cálculo de la sublista paginada se ejecutan en
 * [defaultDispatcher]. Se gestiona la cancelación de trabajos previos ([pageJob]) para evitar ANRs
 * y optimizar la respuesta ante ráfagas de entrada.
 */
class ArtistListViewModel(
    private val repository: ArtistRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArtistListUiState>(ArtistListUiState.Loading)
    val uiState: StateFlow<ArtistListUiState> = _uiState.asStateFlow()

    private var allArtists: List<Performer> = emptyList()
    private var searchQuery: String = ""
    private var filterType: ArtistFilterType = ArtistFilterType.ALL
    private var pageJob: Job? = null

    init {
        loadArtists()
    }

    fun loadArtists() {
        pageJob?.cancel()
        _uiState.value = ArtistListUiState.Loading
        pageJob = viewModelScope.launch {
            _uiState.value = try {
                allArtists = repository.getArtists()
                computePage(page = 1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ArtistListUiState.Error
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        if (searchQuery == query) return
        searchQuery = query
        val current = _uiState.value as? ArtistListUiState.Success ?: return
        _uiState.value = current.copy(searchQuery = query)
        showPage(page = 1)
    }

    fun onFilterTypeChange(type: ArtistFilterType) {
        if (filterType == type) return
        filterType = type
        val current = _uiState.value as? ArtistListUiState.Success ?: return
        _uiState.value = current.copy(filterType = type)
        showPage(page = 1)
    }

    fun nextPage() {
        val state = _uiState.value as? ArtistListUiState.Success ?: return
        if (state.hasNextPage) showPage(state.currentPage + 1)
    }

    fun previousPage() {
        val state = _uiState.value as? ArtistListUiState.Success ?: return
        if (state.hasPreviousPage) showPage(state.currentPage - 1)
    }

    private fun showPage(page: Int) {
        pageJob?.cancel()
        pageJob = viewModelScope.launch {
            _uiState.value = computePage(page)
        }
    }

    private suspend fun computePage(page: Int): ArtistListUiState {
        val artists = allArtists
        val query = searchQuery
        val type = filterType
        return withContext(defaultDispatcher) { pageOf(artists, query, type, page) }
    }

    companion object {
        const val PAGE_SIZE = 10

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ArtistListViewModel(ArtistRepository()) }
        }

        /**
         * Filtra [artists] por término de búsqueda y por [filterType], devolviendo la página [page] (desde 1).
         * La sublista devuelta se expone como una vista sin duplicar elementos en memoria.
         */
        internal fun pageOf(
            artists: List<Performer>,
            query: String,
            filterType: ArtistFilterType,
            page: Int,
        ): ArtistListUiState {
            if (artists.isEmpty()) return ArtistListUiState.Empty

            val term = query.trim()
            val filtered = artists.filter { artist ->
                val matchesType = when (filterType) {
                    ArtistFilterType.ALL -> true
                    ArtistFilterType.BANDS -> artist.isBand
                    ArtistFilterType.MUSICIANS -> !artist.isBand
                }
                val matchesQuery = term.isEmpty() ||
                        artist.name.contains(term, ignoreCase = true) ||
                        artist.description.contains(term, ignoreCase = true)

                matchesType && matchesQuery
            }

            if (filtered.isEmpty()) {
                return ArtistListUiState.Success(
                    artists = emptyList(),
                    totalArtists = 0,
                    currentPage = 1,
                    totalPages = 1,
                    searchQuery = query,
                    filterType = filterType,
                )
            }

            val totalPages = (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE
            val safePage = page.coerceIn(1, totalPages)
            val fromIndex = (safePage - 1) * PAGE_SIZE
            val pageContent = filtered.subList(fromIndex, min(fromIndex + PAGE_SIZE, filtered.size))

            return ArtistListUiState.Success(
                artists = pageContent,
                totalArtists = filtered.size,
                currentPage = safePage,
                totalPages = totalPages,
                searchQuery = query,
                filterType = filterType,
            )
        }
    }
}
