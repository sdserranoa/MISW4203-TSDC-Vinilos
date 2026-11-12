package com.example.misw4203_tsdc_vinilos.ui.screens.collectors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.CollectorRepository
import com.example.misw4203_tsdc_vinilos.model.CollectorSummary
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
 * Carga los coleccionistas registrados y los pagina en el cliente, ya que `GET /collectors`
 * devuelve todos los registros en una sola respuesta (HU05).
 *
 * El filtro por nombre y el cálculo de cada página se ejecutan en [defaultDispatcher]; si llega
 * una nueva búsqueda o un cambio de página antes de terminar, el cálculo anterior se cancela.
 */
class CollectorListViewModel(
    private val repository: CollectorRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectorListUiState>(CollectorListUiState.Loading)
    val uiState: StateFlow<CollectorListUiState> = _uiState.asStateFlow()

    private var allCollectors: List<CollectorSummary> = emptyList()
    private var searchQuery: String = ""
    private var pageJob: Job? = null

    init {
        loadCollectors()
    }

    fun loadCollectors() {
        pageJob?.cancel()
        _uiState.value = CollectorListUiState.Loading
        pageJob = viewModelScope.launch {
            _uiState.value = try {
                allCollectors = repository.getCollectorSummaries()
                computePage(page = 1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CollectorListUiState.Error
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        if (searchQuery == query) return
        searchQuery = query
        val current = _uiState.value as? CollectorListUiState.Success ?: return
        // El campo de búsqueda refleja de inmediato lo escrito; el filtrado se calcula aparte.
        _uiState.value = current.copy(searchQuery = query)
        showPage(page = 1)
    }

    fun nextPage() {
        val state = _uiState.value as? CollectorListUiState.Success ?: return
        if (state.hasNextPage) showPage(state.currentPage + 1)
    }

    fun previousPage() {
        val state = _uiState.value as? CollectorListUiState.Success ?: return
        if (state.hasPreviousPage) showPage(state.currentPage - 1)
    }

    private fun showPage(page: Int) {
        pageJob?.cancel()
        pageJob = viewModelScope.launch {
            _uiState.value = computePage(page)
        }
    }

    private suspend fun computePage(page: Int): CollectorListUiState {
        val collectors = allCollectors
        val query = searchQuery
        return withContext(defaultDispatcher) { pageOf(collectors, query, page) }
    }

    companion object {
        const val PAGE_SIZE = 10

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { CollectorListViewModel(CollectorRepository()) }
        }

        /**
         * Filtra [collectors] por nombre y devuelve la página [page] (desde 1). La página se
         * expone como una vista de la lista filtrada (`subList`), sin copiar los elementos.
         */
        internal fun pageOf(
            collectors: List<CollectorSummary>,
            query: String,
            page: Int,
        ): CollectorListUiState {
            if (collectors.isEmpty()) return CollectorListUiState.Empty
            val term = query.trim()
            val filtered = if (term.isEmpty()) {
                collectors
            } else {
                collectors.filter { it.name.contains(term, ignoreCase = true) }
            }
            val totalPages = if (filtered.isEmpty()) 1 else (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE
            val safePage = page.coerceIn(1, totalPages)
            val fromIndex = (safePage - 1) * PAGE_SIZE
            return CollectorListUiState.Success(
                collectors = filtered.subList(fromIndex, min(fromIndex + PAGE_SIZE, filtered.size)),
                totalCollectors = filtered.size,
                currentPage = safePage,
                totalPages = totalPages,
                searchQuery = query,
            )
        }
    }
}
