package com.example.misw4203_tsdc_vinilos.ui.screens.collectors

import com.example.misw4203_tsdc_vinilos.data.network.RetrofitCollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.repository.CollectorRepository
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import com.example.misw4203_tsdc_vinilos.testutil.testCollectors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/**
 * Pruebas unitarias de la carga, el filtro por nombre y la paginación en cliente del listado de
 * coleccionistas (HU05). Se ejercitan las tres capas reales (ViewModel, Repository y Service
 * Adapter) con un servicio HTTP falso y dispatchers de prueba.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class CollectorListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private fun viewModelWith(service: FakeVinilosApiService) = CollectorListViewModel(
        repository = CollectorRepository(
            serviceAdapter = RetrofitCollectorServiceAdapter(api = service, ioDispatcher = testDispatcher),
            defaultDispatcher = testDispatcher,
        ),
        defaultDispatcher = testDispatcher,
    )

    private fun CollectorListViewModel.successState() = uiState.value as CollectorListUiState.Success

    private val namedCollectors = listOf(
        testCollector(1, "Manolo Bellon", albumCount = 1),
        testCollector(2, "Jaime Monsalve", albumCount = 1),
        testCollector(3, "Manuela Restrepo", albumCount = 4),
    )

    @Test
    fun cargaInicial_conColeccionistas_muestraPrimeraPagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = testCollectors(23)))

        val state = viewModel.successState()
        assertEquals(1, state.currentPage)
        assertEquals(3, state.totalPages)
        assertEquals(23, state.totalCollectors)
        assertEquals((1..10).toList(), state.collectors.map { it.id })
        assertFalse(state.hasPreviousPage)
        assertTrue(state.hasNextPage)
    }

    @Test
    fun cargaInicial_muestraNombreCorreoYCantidadDeAlbumes() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = namedCollectors))

        val manuela = viewModel.successState().collectors.first { it.id == 3 }
        assertEquals("Manuela Restrepo", manuela.name)
        assertEquals("coleccionista3@vinilos.co", manuela.email)
        assertEquals(4, manuela.albumCount)
    }

    @Test
    fun nextPage_hastaUltimaPagina_muestraColeccionistasRestantes() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = testCollectors(23)))

        viewModel.nextPage()
        assertEquals((11..20).toList(), viewModel.successState().collectors.map { it.id })

        viewModel.nextPage()
        val lastPage = viewModel.successState()
        assertEquals(3, lastPage.currentPage)
        assertEquals((21..23).toList(), lastPage.collectors.map { it.id })
        assertFalse(lastPage.hasNextPage)
    }

    @Test
    fun nextPage_enUltimaPagina_noCambiaDePagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = testCollectors(5)))

        viewModel.nextPage()

        assertEquals(1, viewModel.successState().currentPage)
    }

    @Test
    fun previousPage_regresaALaPaginaAnterior() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = testCollectors(23)))
        viewModel.nextPage()

        viewModel.previousPage()

        assertEquals(1, viewModel.successState().currentPage)
        assertEquals((1..10).toList(), viewModel.successState().collectors.map { it.id })
    }

    @Test
    fun previousPage_enPrimeraPagina_noCambiaDePagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = testCollectors(23)))

        viewModel.previousPage()

        assertEquals(1, viewModel.successState().currentPage)
    }

    @Test
    fun cargaInicial_conPaginasCompletas_noGeneraPaginaVacia() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = testCollectors(20)))

        assertEquals(2, viewModel.successState().totalPages)
    }

    @Test
    fun cargaInicial_sinColeccionistas_muestraEstadoVacio() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = emptyList()))

        assertEquals(CollectorListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun cargaInicial_conErrorDeRed_muestraEstadoError() {
        val viewModel = viewModelWith(FakeVinilosApiService(error = IOException("Sin conexión")))

        assertEquals(CollectorListUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun loadCollectors_trasUnError_reintentaYMuestraColeccionistas() {
        val service = FakeVinilosApiService(error = IOException("Sin conexión"))
        val viewModel = viewModelWith(service)

        service.error = null
        service.collectors = testCollectors(3)
        viewModel.loadCollectors()

        assertEquals(3, viewModel.successState().totalCollectors)
    }

    @Test
    fun filtro_porNombre_sinDistinguirMayusculas_filtraLosColeccionistas() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = namedCollectors))

        viewModel.onSearchQueryChange("manu")

        val state = viewModel.successState()
        assertEquals(1, state.totalCollectors)
        assertEquals(listOf(3), state.collectors.map { it.id })
        assertEquals("manu", state.searchQuery)
    }

    @Test
    fun filtro_conPrefijoComun_devuelveTodasLasCoincidencias() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = namedCollectors))

        viewModel.onSearchQueryChange("Man")

        assertEquals(listOf(1, 3), viewModel.successState().collectors.map { it.id })
    }

    @Test
    fun filtro_ignoraEspaciosAlInicioYAlFinal() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = namedCollectors))

        viewModel.onSearchQueryChange("  Jaime  ")

        assertEquals(listOf(2), viewModel.successState().collectors.map { it.id })
    }

    @Test
    fun filtro_noBuscaPorCorreo() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = namedCollectors))

        viewModel.onSearchQueryChange("vinilos.co")

        assertEquals(0, viewModel.successState().totalCollectors)
    }

    @Test
    fun filtro_sinCoincidencias_muestraEstadoExitoConListaVacia() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = namedCollectors))

        viewModel.onSearchQueryChange("Inexistente")

        val state = viewModel.successState()
        assertEquals(0, state.totalCollectors)
        assertTrue(state.collectors.isEmpty())
        assertEquals(1, state.totalPages)
        assertEquals("Inexistente", state.searchQuery)
    }

    @Test
    fun filtro_alLimpiarTexto_muestraTodosLosColeccionistas() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = namedCollectors))
        viewModel.onSearchQueryChange("Jaime")

        viewModel.onSearchQueryChange("")

        assertEquals(3, viewModel.successState().totalCollectors)
    }

    @Test
    fun filtro_alCambiarTexto_reiniciaAPrimeraPagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = testCollectors(25)))
        viewModel.nextPage()
        assertEquals(2, viewModel.successState().currentPage)

        viewModel.onSearchQueryChange("Coleccionista")

        assertEquals(1, viewModel.successState().currentPage)
        assertEquals(25, viewModel.successState().totalCollectors)
    }

    @Test
    fun filtro_conVariasPaginasDeResultados_paginaSobreLosFiltrados() {
        val collectors = testCollectors(30).map {
            if (it.id % 2 == 0) it.copy(name = "Vinilo ${it.id}") else it
        }
        val viewModel = viewModelWith(FakeVinilosApiService(collectors = collectors))

        viewModel.onSearchQueryChange("Vinilo")
        viewModel.nextPage()

        val state = viewModel.successState()
        assertEquals(15, state.totalCollectors)
        assertEquals(2, state.totalPages)
        assertEquals((22..30 step 2).toList(), state.collectors.map { it.id })
    }
}
