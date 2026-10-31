package com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail

import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.testutil.testAlbums
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/** Pruebas unitarias de la carga y la paginación en cliente del detalle de un álbum (HU02). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModelWith(albumId: Int,service: FakeVinilosApiService) =
        AlbumDetailViewModel(albumId,AlbumRepository(service))

    private fun AlbumDetailViewModel.successState() = uiState.value as AlbumDetailUiState.Success

    @Test
    fun cargaInicial_conExito_emiteEstadoSuccessConAlbum() {
        val album = testAlbum(id = 100, name = "Abbey Road", genre = "Rock", recordLabel = "Apple Records")
        val viewModel = viewModelWith(100, FakeVinilosApiService(albums = listOf(album)))

        val state = viewModel.successState()
        assertEquals(100, state.album.id)
        assertEquals("Abbey Road", state.album.name)
        assertEquals("Apple Records", state.album.recordLabel)
    }

    @Test
    fun cargaInicial_informaciónAlbum() {
        val viewModel = viewModelWith(1,FakeVinilosApiService(albums = testAlbums(5)))

        val state = viewModel.successState()
        assertEquals(1, state.album.id)
        assertEquals("Álbum 1", state.album.name)
        assertEquals("Rock", state.album.genre)
        assertEquals("1984-08-01T00:00:00.000Z", state.album.releaseDate)
    }

    @Test
    fun cargaInicial_conErrorDeRed_muestraEstadoError() {
        val viewModel = viewModelWith(
            1,
            FakeVinilosApiService(error = IOException("Error de red"))
        )

        assertEquals(AlbumDetailUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun loadAlbum_trasUnError_reintentaYCargaAlbum() {
        val service = FakeVinilosApiService(error = IOException("Error de red"))
        val viewModel = viewModelWith(1, service)

        // Verificamos que arrancó en estado Error
        assertEquals(AlbumDetailUiState.Error, viewModel.uiState.value)

        // Corregimos el servicio y reintentamos
        service.error = null
        service.albums = testAlbums(3)
        viewModel.loadAlbum()

        val state = viewModel.uiState.value as AlbumDetailUiState.Success
        assertEquals(1, state.album.id)
        assertEquals("Álbum 1", state.album.name)
    }

    @Test
    fun cargaInicial_albumNoEncontrado_muestraEstadoError() {
        val viewModel = viewModelWith(
            999,
            FakeVinilosApiService(albums = testAlbums(2))
        )

        assertEquals(AlbumDetailUiState.Error, viewModel.uiState.value)
    }
}