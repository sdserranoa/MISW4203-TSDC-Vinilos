package com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.CollectorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Carga el detalle del coleccionista identificado por [collectorId] (HU06): sus datos, sus álbumes,
 * sus artistas favoritos y sus comentarios. La carga se cancela si el usuario sale de la pantalla.
 */
class CollectorDetailViewModel(
    private val collectorId: Int,
    private val repository: CollectorRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectorDetailUiState>(CollectorDetailUiState.Loading)
    val uiState: StateFlow<CollectorDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadCollector()
    }

    fun loadCollector() {
        loadJob?.cancel()
        _uiState.value = CollectorDetailUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = try {
                CollectorDetailUiState.Success(repository.getCollectorDetail(collectorId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CollectorDetailUiState.Error
            }
        }
    }

    companion object {
        fun provideFactory(
            collectorId: Int,
            repository: CollectorRepository = CollectorRepository(),
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CollectorDetailViewModel(
                    collectorId = collectorId,
                    repository = repository,
                )
            }
        }
    }
}
