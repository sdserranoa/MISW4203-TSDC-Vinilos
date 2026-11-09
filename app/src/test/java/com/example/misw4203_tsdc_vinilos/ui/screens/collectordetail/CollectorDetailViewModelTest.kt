package com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail

import com.example.misw4203_tsdc_vinilos.data.network.RetrofitCollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.repository.CollectorRepository
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import com.example.misw4203_tsdc_vinilos.testutil.testCollectorAlbum
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/**
 * Pruebas unitarias de la carga del detalle de un coleccionista (HU06), a través de las capas
 * reales (ViewModel, Repository y Service Adapter) con un servicio HTTP falso.
 */
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

    private fun CollectorDetailViewModel.successState() = uiState.value as CollectorDetailUiState.Success

    private val serviceWithManolo = FakeVinilosApiService(
        collectors = listOf(
            testCollector(
                id = 100,
                name = "Manolo Bellon",
                favoritePerformers = listOf(Performer(100, "Rubén Blades Bellido de Luna")),
                comments = listOf(Comment(100, "The most relevant album of Ruben Blades", 5)),
            ),
            testCollector(101, "Jaime Monsalve"),
        ),
        collectorAlbums = mapOf(
            100 to listOf(
                testCollectorAlbum(testAlbum(100, name = "Buscando América", genre = "Salsa")),
                testCollectorAlbum(testAlbum(101, name = "Poeta del pueblo", genre = "Salsa")),
            ),
        ),
    )

    @Test
    fun cargaInicial_muestraDatosAlbumesFavoritosYComentariosDelColeccionista() {
        val viewModel = viewModelWith(100, serviceWithManolo)

        val collector = viewModel.successState().collector
        assertEquals("Manolo Bellon", collector.name)
        assertEquals(listOf("Buscando América", "Poeta del pueblo"), collector.albums.map { it.name })
        assertEquals(listOf("Rubén Blades Bellido de Luna"), collector.favoritePerformers.map { it.name })
        assertEquals(listOf("The most relevant album of Ruben Blades"), collector.comments.map { it.description })
    }

    @Test
    fun cargaInicial_coleccionistaSinRegistros_muestraListasVacias() {
        val viewModel = viewModelWith(101, serviceWithManolo)

        val collector = viewModel.successState().collector
        assertEquals("Jaime Monsalve", collector.name)
        assertTrue(collector.albums.isEmpty())
        assertTrue(collector.favoritePerformers.isEmpty())
        assertTrue(collector.comments.isEmpty())
    }

    @Test
    fun cargaInicial_conErrorDeRed_muestraEstadoError() {
        val viewModel = viewModelWith(100, FakeVinilosApiService(error = IOException("Sin conexión")))

        assertEquals(CollectorDetailUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun cargaInicial_conColeccionistaInexistente_muestraEstadoError() {
        val viewModel = viewModelWith(999, serviceWithManolo)

        assertEquals(CollectorDetailUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun loadCollector_trasUnError_reintentaYMuestraElColeccionista() {
        val service = FakeVinilosApiService(error = IOException("Sin conexión"))
        val viewModel = viewModelWith(100, service)

        service.error = null
        service.collectors = serviceWithManolo.collectors
        service.collectorAlbums = serviceWithManolo.collectorAlbums
        viewModel.loadCollector()

        assertEquals(2, viewModel.successState().collector.albums.size)
    }
}
