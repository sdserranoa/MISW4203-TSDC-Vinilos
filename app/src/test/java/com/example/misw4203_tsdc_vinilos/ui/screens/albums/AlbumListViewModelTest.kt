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
import java.io.IOException

/** Pruebas unitarias de la carga y la paginación en cliente del catálogo de álbumes (HU01). */
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
}
