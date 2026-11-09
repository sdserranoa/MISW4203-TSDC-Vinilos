package com.example.misw4203_tsdc_vinilos.ui.screens.collectors

import com.example.misw4203_tsdc_vinilos.model.CollectorSummary

/** Estados posibles de la pantalla del listado de coleccionistas (HU05). */
sealed interface CollectorListUiState {

    data object Loading : CollectorListUiState

    /** No hay coleccionistas registrados en el backend. */
    data object Empty : CollectorListUiState

    data object Error : CollectorListUiState

    /**
     * @property collectors coleccionistas de la página actual.
     * @property totalCollectors cantidad de coleccionistas que cumplen el filtro actual.
     * @property currentPage página actual, empezando en 1.
     * @property totalPages cantidad de páginas para el filtro actual.
     * @property searchQuery texto actual del filtro por nombre.
     */
    data class Success(
        val collectors: List<CollectorSummary>,
        val totalCollectors: Int,
        val currentPage: Int,
        val totalPages: Int,
        val searchQuery: String = "",
    ) : CollectorListUiState {
        val hasPreviousPage: Boolean
            get() = currentPage > 1
        val hasNextPage: Boolean
            get() = currentPage < totalPages
    }
}
