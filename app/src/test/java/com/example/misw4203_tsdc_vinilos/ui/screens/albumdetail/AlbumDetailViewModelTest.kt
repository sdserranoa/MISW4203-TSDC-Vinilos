package com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail

import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/** Pruebas unitarias para la carga del detalle del álbum (HU02). */
class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModelWith(albumId: Int, service: FakeVinilosApiService) =
        AlbumDetailViewModel(albumId = albumId, repository = AlbumRepository(service))

    @Test
    fun cargaInicial_conExito_emiteEstadoSuccessConAlbum() {
        val album = testAlbum(id = 100, name = "Abbey Road", genre = "Rock", recordLabel = "Apple Records")
        val service = FakeVinilosApiService(albums = listOf(album))
        val viewModel = viewModelWith(albumId = 100, service = service)

        val state = viewModel.uiState.value
        assertTrue(state is AlbumDetailUiState.Success)
        val successState = state as AlbumDetailUiState.Success
        assertEquals(100, successState.album.id)
        assertEquals("Abbey Road", successState.album.name)
        assertEquals("Apple Records", successState.album.recordLabel)
    }

    @Test
    fun cargaInicial_conError_emiteEstadoError() {
        val service = FakeVinilosApiService(error = IOException("Network error"))
        val viewModel = viewModelWith(albumId = 100, service = service)

        val state = viewModel.uiState.value
        assertTrue(state is AlbumDetailUiState.Error)
    }

    @Test
    fun reintento_trasError_recuperaEstadoSuccess() {
        val service = FakeVinilosApiService(error = IOException("Network error"))
        val viewModel = viewModelWith(albumId = 100, service = service)

        assertTrue(viewModel.uiState.value is AlbumDetailUiState.Error)

        val album = testAlbum(id = 100, name = "Revolver")
        service.error = null
        service.albums = listOf(album)

        viewModel.loadAlbum()

        val state = viewModel.uiState.value
        assertTrue(state is AlbumDetailUiState.Success)
        assertEquals("Revolver", (state as AlbumDetailUiState.Success).album.name)
    }
}
