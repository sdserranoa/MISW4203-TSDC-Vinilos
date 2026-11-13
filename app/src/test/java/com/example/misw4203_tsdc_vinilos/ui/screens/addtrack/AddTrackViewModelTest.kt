package com.example.misw4203_tsdc_vinilos.ui.screens.addtrack

import com.example.misw4203_tsdc_vinilos.data.network.RetrofitAlbumServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class AddTrackViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private fun viewModelWith(service: FakeVinilosApiService, albumId: Int = 1) = AddTrackViewModel(
        albumId = albumId,
        repository = AlbumRepository(
            serviceAdapter = RetrofitAlbumServiceAdapter(api = service, ioDispatcher = testDispatcher),
        ),
    )

    private fun AddTrackViewModel.formState() = uiState.value as AddTrackUiState.Form

    @Test
    fun cargaInicial_conExito_cargaEncabezadoDelAlbumYFormulario() {
        val album = testAlbum(1, name = "Abbey Road")
        val viewModel = viewModelWith(FakeVinilosApiService(albums = listOf(album)))

        val state = viewModel.formState()
        assertEquals("Abbey Road", state.album.name)
        assertFalse(state.isValid)
    }

    @Test
    fun validacion_campoNombreYDuracion_habilitaBotonSoloCuandoAmbosSonValidos() {
        val album = testAlbum(1, name = "Abbey Road")
        val viewModel = viewModelWith(FakeVinilosApiService(albums = listOf(album)))

        // Nombre solo -> invalido
        viewModel.onNameChange("Come Together")
        assertFalse(viewModel.formState().isValid)

        // Duración inválida -> invalido
        viewModel.onDurationChange("inválido")
        assertFalse(viewModel.formState().isValid)

        // Duración válida -> valido
        viewModel.onDurationChange("04:20")
        assertTrue(viewModel.formState().isValid)
    }

    @Test
    fun envioFormulario_conExito_invocaOnSuccessNavBack() {
        val album = testAlbum(1, name = "Abbey Road")
        val viewModel = viewModelWith(FakeVinilosApiService(albums = listOf(album)))
        var successNavigated = false
        var errorNavigated = false

        viewModel.onNameChange("Come Together")
        viewModel.onDurationChange("04:20")

        viewModel.submitTrack(
            onSuccessNavBack = { successNavigated = true },
            onErrorNavBack = { errorNavigated = true },
        )

        assertTrue(successNavigated)
        assertFalse(errorNavigated)
        assertTrue(viewModel.formState().isSuccess)
    }

    @Test
    fun envioFormulario_conError_invocaOnErrorNavBack() {
        val album = testAlbum(1, name = "Abbey Road")
        val viewModel = viewModelWith(
            FakeVinilosApiService(
                albums = listOf(album),
                postTrackError = IOException("Red no disponible"),
            )
        )
        var successNavigated = false
        var errorNavigated = false

        viewModel.onNameChange("Come Together")
        viewModel.onDurationChange("04:20")

        viewModel.submitTrack(
            onSuccessNavBack = { successNavigated = true },
            onErrorNavBack = { errorNavigated = true },
        )

        assertFalse(successNavigated)
        assertTrue(errorNavigated)
    }
}
