package com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail

import com.example.misw4203_tsdc_vinilos.data.network.RetrofitCollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.repository.CollectorRepository
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/** Pruebas unitarias de la carga del detalle de un coleccionista. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class CollectorDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private fun viewModelWith(collectorId: Int, service: FakeVinilosApiService) = CollectorDetailViewModel(
        collectorId = collectorId,
        repository = CollectorRepository(
            serviceAdapter = RetrofitCollectorServiceAdapter(api = service, ioDispatcher = testDispatcher),
            defaultDispatcher = testDispatcher,
        ),
    )

    @Test
    fun cargaInicial_muestraElColeccionistaSolicitado() {
        val service = FakeVinilosApiService(
            collectors = listOf(testCollector(100, "Manolo Bellon"), testCollector(101, "Jaime Monsalve")),
        )

        val viewModel = viewModelWith(101, service)

        val state = viewModel.uiState.value as CollectorDetailUiState.Success
        assertEquals("Jaime Monsalve", state.collector.name)
    }

    @Test
    fun cargaInicial_conErrorDeRed_muestraEstadoError() {
        val viewModel = viewModelWith(100, FakeVinilosApiService(error = IOException("Sin conexión")))

        assertEquals(CollectorDetailUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun cargaInicial_conColeccionistaInexistente_muestraEstadoError() {
        val viewModel = viewModelWith(999, FakeVinilosApiService(collectors = listOf(testCollector(100))))

        assertEquals(CollectorDetailUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun loadCollector_trasUnError_reintentaYMuestraElColeccionista() {
        val service = FakeVinilosApiService(error = IOException("Sin conexión"))
        val viewModel = viewModelWith(100, service)

        service.error = null
        service.collectors = listOf(testCollector(100, "Manolo Bellon"))
        viewModel.loadCollector()

        val state = viewModel.uiState.value as CollectorDetailUiState.Success
        assertEquals("Manolo Bellon", state.collector.name)
    }
}
