package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Pruebas unitarias de la creación de un nuevo álbum (HU07). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class AlbumCreateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModelWith(service: FakeVinilosApiService = FakeVinilosApiService()) =
        AlbumCreateViewModel(AlbumRepository(service))

    private fun AlbumCreateViewModel.successState() = uiState.value as AlbumCreateUiState.Success

    private fun AlbumCreateViewModel.fillValidAlbum() {
        onNameChange("Álbum 2024")
        onCoverChange("https://example.com/cover.jpg")
        onReleaseDateChange("2024-02-29")
        onGenreChange("Rock")
        onRecordLabelChange("Sony Music")
        onDescriptionChange("Música clásica, edición 2024.")
    }

    @Test
    fun estadoInicial_contieneCamposVacios() {
        val viewModel = viewModelWith()

        val state = viewModel.successState()
        assertTrue(state.album.name.isEmpty())
        assertTrue(state.album.cover.isEmpty())
        assertTrue(state.album.releaseDate.isEmpty())
        assertTrue(state.album.genre.isEmpty())
        assertTrue(state.album.recordLabel.isEmpty())
        assertTrue(state.album.description.isEmpty())
        assertFalse(state.showSnackbar)
        assertFalse(state.isSaving)
        assertTrue(state.validationErrors.isEmpty())
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

        val text100 = "A".repeat(100)
        viewModel.onNameChange(text100)
        assertEquals(text100, viewModel.album.name)

        val supplementaryLetter = "\uD801\uDC00"
        val text100CodePoints = supplementaryLetter.repeat(100)
        viewModel.onNameChange(text100CodePoints)
        assertEquals(text100CodePoints, viewModel.album.name)
        viewModel.onNameChange(supplementaryLetter.repeat(101))
        assertEquals(text100CodePoints, viewModel.album.name)
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
        viewModel.fillValidAlbum()

        var savedSuccess = false
        viewModel.saveAlbum {
            savedSuccess = true
        }

        val state = viewModel.successState()
        assertTrue(state.isCreated)
        assertTrue(state.showSnackbar)
        assertTrue(savedSuccess)
        assertEquals(1, service.albums.size)
    }

    @Test
    fun guardarAlbum_conError_notificaFalloSinConfirmarCreacion() {
        val errors = listOf(
            IOException("Network unavailable"),
            HttpException(Response.error<Album>(500, "Server error".toResponseBody())),
        )

        errors.forEach { error ->
            val service = FakeVinilosApiService(error = error)
            val viewModel = viewModelWith(service)
            viewModel.fillValidAlbum()
            var saved = false
            var failed = false

            viewModel.saveAlbum(
                onSaveFailed = { failed = true },
                onSaved = { saved = true },
            )

            assertEquals(AlbumCreateUiState.SaveError, viewModel.uiState.value)
            assertTrue(failed)
            assertFalse(saved)
            assertEquals(0, viewModel.album.id)
            assertTrue(service.albums.isEmpty())
        }
    }

    @Test
    fun guardarAlbum_cancelado_noNotificaExitoNiError() {
        val service = FakeVinilosApiService(error = CancellationException("Cancelled"))
        val viewModel = viewModelWith(service)
        viewModel.fillValidAlbum()
        var saved = false
        var failed = false

        viewModel.saveAlbum(
            onSaveFailed = { failed = true },
            onSaved = { saved = true },
        )

        assertFalse(saved)
        assertFalse(failed)
        assertFalse(viewModel.successState().isCreated)
        assertFalse(viewModel.successState().showSnackbar)
        assertFalse(viewModel.successState().isSaving)
        assertTrue(service.albums.isEmpty())
    }

    @Test
    fun descartarSnackbar_ocultaSnackbar() {
        val viewModel = viewModelWith()
        viewModel.fillValidAlbum()

        viewModel.saveAlbum()
        assertTrue(viewModel.successState().showSnackbar)

        viewModel.dismissSnackbar()
        assertFalse(viewModel.successState().showSnackbar)
    }

    @Test
    fun nuevoAlbum_reiniciaCamposYErrores() {
        val viewModel = viewModelWith()

        viewModel.onNameChange("Nombre Personalizado")
        assertEquals("Nombre Personalizado", viewModel.album.name)
        viewModel.saveAlbum()
        assertFalse(viewModel.successState().validationErrors.isEmpty())

        viewModel.newAlbum()
        assertTrue(viewModel.album.name.isEmpty())
        assertTrue(viewModel.successState().validationErrors.isEmpty())
    }

    @Test
    fun guardarAlbum_conCadaCampoVacio_noLlamaAlServicio() {
        val missingFields = listOf<Pair<AlbumCreateField, (AlbumCreateViewModel) -> Unit>>(
            AlbumCreateField.NAME to { it.onNameChange("  ") },
            AlbumCreateField.COVER to { it.onCoverChange("  ") },
            AlbumCreateField.RELEASE_DATE to { it.onReleaseDateChange("  ") },
            AlbumCreateField.GENRE to { it.onGenreChange("  ") },
            AlbumCreateField.RECORD_LABEL to { it.onRecordLabelChange("  ") },
            AlbumCreateField.DESCRIPTION to { it.onDescriptionChange("  ") },
        )

        missingFields.forEach { (field, clearField) ->
            val service = FakeVinilosApiService()
            val viewModel = viewModelWith(service)
            viewModel.fillValidAlbum()
            clearField(viewModel)

            var saved = false
            viewModel.saveAlbum { saved = true }

            assertEquals(
                mapOf(field to AlbumCreateValidationError.REQUIRED),
                viewModel.successState().validationErrors,
            )
            assertTrue(service.albums.isEmpty())
            assertFalse(saved)
        }
    }

    @Test
    fun guardarAlbum_textoInvalidoSeCorrigeYPermitePuntuacionYAcentos() {
        val service = FakeVinilosApiService()
        val viewModel = viewModelWith(service)
        viewModel.fillValidAlbum()
        viewModel.onNameChange("...")
        viewModel.onDescriptionChange("Música 🎵")

        viewModel.saveAlbum()

        assertEquals(
            mapOf(
                AlbumCreateField.NAME to AlbumCreateValidationError.INVALID_TEXT,
                AlbumCreateField.DESCRIPTION to AlbumCreateValidationError.INVALID_TEXT,
            ),
            viewModel.successState().validationErrors,
        )
        assertTrue(service.albums.isEmpty())

        viewModel.onNameChange("Cancio\u0301n 1: edición — 2024")
        viewModel.onDescriptionChange("Música clásica, volumen 1 (edición 2024).")
        assertTrue(viewModel.successState().validationErrors.isEmpty())

        viewModel.saveAlbum()
        assertEquals(1, service.albums.size)
    }

    @Test
    fun guardarAlbum_rechazaNombreMayorAlLimiteYAcepta100Caracteres() {
        val service = FakeVinilosApiService()
        val viewModel = viewModelWith(service)
        viewModel.fillValidAlbum()
        viewModel.onNameChange("")
        viewModel.onNameChange("A".repeat(101))

        viewModel.saveAlbum()
        assertTrue(service.albums.isEmpty())
        assertEquals(
            AlbumCreateValidationError.REQUIRED,
            viewModel.successState().validationErrors[AlbumCreateField.NAME],
        )

        val name = "A".repeat(100)
        viewModel.onNameChange(name)
        viewModel.saveAlbum()
        assertEquals(name, service.albums.single().name)
    }

    @Test
    fun guardarAlbum_validaUrlYNormalizaEspaciosAlrededor() {
        val service = FakeVinilosApiService()
        val viewModel = viewModelWith(service)
        viewModel.fillValidAlbum()

        listOf(
            "cover.jpg",
            "ftp://example.com/cover.jpg",
            "https://",
            "https:///example.com",
            "https://example.com/a b",
            "https://example.com/%zz",
        )
            .forEach { invalidUrl ->
                viewModel.onCoverChange(invalidUrl)
                viewModel.saveAlbum()
                assertEquals(
                    AlbumCreateValidationError.INVALID_URL,
                    viewModel.successState().validationErrors[AlbumCreateField.COVER],
                )
                assertTrue(service.albums.isEmpty())
            }

        viewModel.onCoverChange("  https://example.com/cover%20art.jpg  ")
        viewModel.saveAlbum()

        assertEquals("https://example.com/cover%20art.jpg", service.albums.single().cover)
    }

    @Test
    fun guardarAlbum_rechazaFechasInvalidas() {
        val service = FakeVinilosApiService()
        val viewModel = viewModelWith(service)
        viewModel.fillValidAlbum()

        listOf("2024-02-30", "2024-2-29", "no es una fecha").forEach { invalidDate ->
            viewModel.onReleaseDateChange(invalidDate)
            viewModel.saveAlbum()
            assertEquals(
                AlbumCreateValidationError.INVALID_DATE,
                viewModel.successState().validationErrors[AlbumCreateField.RELEASE_DATE],
            )
            assertTrue(service.albums.isEmpty())
        }

        viewModel.onReleaseDateChange("2024-02-29")
        viewModel.saveAlbum()
        assertEquals(1, service.albums.size)
    }

    @Test
    fun guardarAlbum_rechazaGeneroYSelloQueNoAceptaElServidor() {
        val service = FakeVinilosApiService()
        val viewModel = viewModelWith(service)
        viewModel.fillValidAlbum()
        viewModel.onGenreChange("Jazz")
        viewModel.onRecordLabelChange("Blue Note")

        viewModel.saveAlbum()

        val errors = viewModel.successState().validationErrors
        assertEquals(AlbumCreateValidationError.INVALID_OPTION, errors[AlbumCreateField.GENRE])
        assertEquals(AlbumCreateValidationError.INVALID_OPTION, errors[AlbumCreateField.RECORD_LABEL])
        assertTrue(service.albums.isEmpty())

        viewModel.onGenreChange("Classical")
        viewModel.onRecordLabelChange("Fania Records")
        viewModel.saveAlbum()
        assertEquals(1, service.albums.size)
    }
}
