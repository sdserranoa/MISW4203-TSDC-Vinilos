package com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail

import com.example.misw4203_tsdc_vinilos.model.CollectorDetail

/** Estados posibles de la pantalla de detalle de un coleccionista (HU06). */
sealed interface CollectorDetailUiState {

    data object Loading : CollectorDetailUiState

    data object Error : CollectorDetailUiState

    data class Success(val collector: CollectorDetail) : CollectorDetailUiState
}
