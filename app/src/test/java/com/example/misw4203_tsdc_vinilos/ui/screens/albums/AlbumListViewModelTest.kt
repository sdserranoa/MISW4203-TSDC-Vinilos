package com.example.misw4203_tsdc_vinilos.ui.screens.albums

import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import com.example.misw4203_tsdc_vinilos.testutil.testAlbums
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/** Pruebas unitarias de la carga y la paginación en cliente del catálogo de álbumes (HU01). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class AlbumListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModelWith(service: FakeVinilosApiService) =
        AlbumListViewModel(AlbumRepository(service))

    private fun AlbumListViewModel.successState() = uiState.value as AlbumListUiState.Success

    @Test
    fun cargaInicial_conAlbumes_muestraPrimeraPagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(albums = testAlbums(23)))

        val state = viewModel.successState()
        assertEquals(1, state.currentPage)
        assertEquals(3, state.totalPages)
        assertEquals(23, state.totalAlbums)
        assertEquals((1..10).toList(), state.albums.map { it.id })
        assertFalse(state.hasPreviousPage)
        assertTrue(state.hasNextPage)
    }

    @Test
    fun nextPage_hastaUltimaPagina_muestraAlbumesRestantes() {
        val viewModel = viewModelWith(FakeVinilosApiService(albums = testAlbums(23)))

        viewModel.nextPage()
        assertEquals((11..20).toList(), viewModel.successState().albums.map { it.id })

        viewModel.nextPage()
        val lastPage = viewModel.successState()
        assertEquals(3, lastPage.currentPage)
        assertEquals((21..23).toList(), lastPage.albums.map { it.id })
        assertFalse(lastPage.hasNextPage)
    }

    @Test
    fun nextPage_enUltimaPagina_noCambiaDePagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(albums = testAlbums(5)))

        viewModel.nextPage()

        assertEquals(1, viewModel.successState().currentPage)
    }

    @Test
    fun previousPage_regresaALaPaginaAnterior() {
        val viewModel = viewModelWith(FakeVinilosApiService(albums = testAlbums(23)))
        viewModel.nextPage()

        viewModel.previousPage()

        assertEquals(1, viewModel.successState().currentPage)
        assertEquals((1..10).toList(), viewModel.successState().albums.map { it.id })
    }

    @Test
    fun previousPage_enPrimeraPagina_noCambiaDePagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(albums = testAlbums(23)))

        viewModel.previousPage()

        assertEquals(1, viewModel.successState().currentPage)
    }

    @Test
    fun cargaInicial_conPaginasCompletas_noGeneraPaginaVacia() {
        val viewModel = viewModelWith(FakeVinilosApiService(albums = testAlbums(20)))

        assertEquals(2, viewModel.successState().totalPages)
    }

    @Test
    fun cargaInicial_sinAlbumes_muestraEstadoVacio() {
        val viewModel = viewModelWith(FakeVinilosApiService(albums = emptyList()))

        assertEquals(AlbumListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun cargaInicial_conErrorDeRed_muestraEstadoError() {
        val viewModel = viewModelWith(FakeVinilosApiService(error = IOException("Sin conexión")))

        assertEquals(AlbumListUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun loadAlbums_trasUnError_reintentaYMuestraAlbumes() {
        val service = FakeVinilosApiService(error = IOException("Sin conexión"))
        val viewModel = viewModelWith(service)

        service.error = null
        service.albums = testAlbums(3)
        viewModel.loadAlbums()

        assertEquals(3, viewModel.successState().totalAlbums)
    }

    @Test
    fun filtro_porNombre_filtraLosAlbumesCorrectamente() {
        val albums = listOf(
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(1, name = "Buscando América", genre = "Salsa"),
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(2, name = "Artaud", genre = "Rock"),
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(3, name = "Siembra", genre = "Salsa"),
        )
        val viewModel = viewModelWith(FakeVinilosApiService(albums = albums))

        viewModel.onSearchQueryChange("Buscando")

        val state = viewModel.successState()
        assertEquals(1, state.totalAlbums)
        assertEquals(listOf(1), state.albums.map { it.id })
    }

    @Test
    fun filtro_porGenero_filtraLosAlbumesCorrectamente() {
        val albums = listOf(
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(1, name = "Buscando América", genre = "Salsa"),
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(2, name = "Artaud", genre = "Rock"),
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(3, name = "Siembra", genre = "Salsa"),
        )
        val viewModel = viewModelWith(FakeVinilosApiService(albums = albums))

        viewModel.onSearchQueryChange("salsa")

        val state = viewModel.successState()
        assertEquals(2, state.totalAlbums)
        assertEquals(listOf(1, 3), state.albums.map { it.id })
    }

    @Test
    fun filtro_porArtista_filtraLosAlbumesCorrectamente() {
        val albums = listOf(
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(
                1, name = "Buscando América", genre = "Salsa",
                performers = listOf(com.example.misw4203_tsdc_vinilos.model.Performer(1, "Rubén Blades"))
            ),
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(
                2, name = "Artaud", genre = "Rock",
                performers = listOf(com.example.misw4203_tsdc_vinilos.model.Performer(2, "Pescado Rabioso"))
            ),
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(
                3, name = "Siembra", genre = "Salsa",
                performers = listOf(com.example.misw4203_tsdc_vinilos.model.Performer(1, "Rubén Blades"))
            ),
        )
        val viewModel = viewModelWith(FakeVinilosApiService(albums = albums))

        viewModel.onSearchQueryChange("blades")

        val state = viewModel.successState()
        assertEquals(2, state.totalAlbums)
        assertEquals(listOf(1, 3), state.albums.map { it.id })
    }

    @Test
    fun filtro_sinCoincidencias_muestraEstadoExitoConListaVacia() {
        val albums = listOf(
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(1, name = "Buscando América"),
        )
        val viewModel = viewModelWith(FakeVinilosApiService(albums = albums))

        viewModel.onSearchQueryChange("Inexistente")

        val state = viewModel.successState()
        assertEquals(0, state.totalAlbums)
        assertTrue(state.albums.isEmpty())
        assertEquals("Inexistente", state.searchQuery)
    }

    @Test
    fun filtro_alCambiarTexto_reiniciaAPrimeraPagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(albums = testAlbums(25)))
        viewModel.nextPage()
        assertEquals(2, viewModel.successState().currentPage)

        viewModel.onSearchQueryChange("Álbum")

        assertEquals(1, viewModel.successState().currentPage)
    }

    @Test
    fun filtro_alLimpiarTexto_muestraTodosLosAlbumes() {
        val albums = listOf(
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(1, name = "Buscando América"),
            com.example.misw4203_tsdc_vinilos.testutil.testAlbum(2, name = "Artaud"),
        )
        val viewModel = viewModelWith(FakeVinilosApiService(albums = albums))

        viewModel.onSearchQueryChange("Artaud")
        assertEquals(1, viewModel.successState().totalAlbums)

        viewModel.onSearchQueryChange("")
        assertEquals(2, viewModel.successState().totalAlbums)
    }
}
