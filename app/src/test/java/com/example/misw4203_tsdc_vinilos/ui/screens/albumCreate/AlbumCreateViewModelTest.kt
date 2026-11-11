package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Pruebas unitarias de la creación de un nuevo álbum (HU07). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class AlbumCreateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModelWith(service: FakeVinilosApiService = FakeVinilosApiService()) =
        AlbumCreateViewModel(AlbumRepository(service))

    private fun AlbumCreateViewModel.successState() = uiState.value as AlbumCreateUiState.Success

    @Test
    fun estadoInicial_contieneValoresPredeterminados() {
        val viewModel = viewModelWith()

        val state = viewModel.successState()
        assertEquals("Abbey Road", state.album.name)
        assertEquals("Rock Clásico", state.album.genre)
        assertEquals("Apple Records / EMI", state.album.recordLabel)
        assertFalse(state.showSnackbar)
        assertFalse(state.isSaving)
    }

    @Test
    fun cambioDeNombre_actualizaEstadoYRespetaLimite100Caracteres() {
        val viewModel = viewModelWith()

        viewModel.onNameChange("A Night at the Opera")
        assertEquals("A Night at the Opera", viewModel.album.name)

        // Intento superar el límite de 100 caracteres
        val text105 = "A".repeat(105)
        viewModel.onNameChange(text105)
        assertEquals("A Night at the Opera", viewModel.album.name)
    }

    @Test
    fun cambioDeCampos_actualizaEstadoCorrectamente() {
        val viewModel = viewModelWith()

        viewModel.onCoverChange("https://images.vinilos.app/covers/opera.jpg")
        viewModel.onReleaseDateChange("1975-11-21")
        viewModel.onGenreChange("Rock")
        viewModel.onRecordLabelChange("EMI")
        viewModel.onDescriptionChange("Cuarto álbum de estudio de Queen.")

        val album = viewModel.album
        assertEquals("https://images.vinilos.app/covers/opera.jpg", album.cover)
        assertEquals("1975-11-21", album.releaseDate)
        assertEquals("Rock", album.genre)
        assertEquals("EMI", album.recordLabel)
        assertEquals("Cuarto álbum de estudio de Queen.", album.description)
    }

    @Test
    fun guardarAlbum_conExito_muestraSnackbarYMarcaEstadoCreado() {
        val service = FakeVinilosApiService()
        val viewModel = viewModelWith(service)

        var savedSuccess = false
        viewModel.saveAlbum {
            savedSuccess = true
        }

        val state = viewModel.successState()
        assertTrue(state.isCreated)
        assertTrue(state.showSnackbar)
        assertTrue(savedSuccess)
    }

    @Test
    fun descartarSnackbar_ocultaSnackbar() {
        val viewModel = viewModelWith()

        viewModel.saveAlbum()
        assertTrue(viewModel.successState().showSnackbar)

        viewModel.dismissSnackbar()
        assertFalse(viewModel.successState().showSnackbar)
    }

    @Test
    fun nuevoAlbum_reiniciaValoresPredeterminados() {
        val viewModel = viewModelWith()

        viewModel.onNameChange("Nombre Personalizado")
        assertEquals("Nombre Personalizado", viewModel.album.name)

        viewModel.newAlbum()
        assertEquals("Abbey Road", viewModel.album.name)
    }
}
