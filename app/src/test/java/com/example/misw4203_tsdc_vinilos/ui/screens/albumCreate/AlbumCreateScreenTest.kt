package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de interfaz en Jetpack Compose para el formulario de creación de álbum (HU07).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class AlbumCreateScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: AlbumCreateUiState,
        onBack: () -> Unit = {},
        onAlbumSubmit: () -> Unit = {},
        onRetry: () -> Unit = {},
        onSaveAlbum: () -> Unit = {},
        onDismissSnackbar: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                AlbumCreateContent(
                    uiState = uiState,
                    onBack = onBack,
                    onAlbumSubmit = onAlbumSubmit,
                    onRetry = onRetry,
                    onSaveAlbum = onSaveAlbum,
                    onDismissSnackbar = onDismissSnackbar,
                )
            }
        }
    }

    @Test
    fun formularioExito_muestraBannersCamposYAcciones() {
        val testAlbum = Album(
            id = 0,
            name = "Abbey Road",
            cover = "https://images.vinilos.app/covers/abbey-road.jpg",
            releaseDate = "1969-09-26",
            description = "Undécimo álbum de estudio de The Beatles.",
            genre = "Rock",
            recordLabel = "EMI",
        )

        setContent(AlbumCreateUiState.Success(album = testAlbum))

        composeTestRule.onNodeWithText("Nuevo Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("INFORMACIÓN DEL VINILO").assertIsDisplayed()
        composeTestRule.onNodeWithText("HU07").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cambiar carátula").assertDoesNotExist()

        listOf("Fecha de lanzamiento", "Género musical", "Sello discográfico", "Audio Hi-Res Vinilo")
            .forEach { label ->
                composeTestRule.onNodeWithText(label).performScrollTo().assertIsDisplayed()
            }

        composeTestRule.onNodeWithText("Guardar Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun cargando_muestraMensajeDeCarga() {
        setContent(AlbumCreateUiState.Loading)

        composeTestRule.onNodeWithText("Cargando formulario de creación del álbum…").assertIsDisplayed()
    }

    @Test
    fun error_muestraMensajeDeErrorYBotonReintentar() {
        var retried = false
        setContent(
            AlbumCreateUiState.Error,
            onRetry = { retried = true },
        )

        composeTestRule.onNodeWithText(
            "Ocurrió un error al cargar el formulario. Por favor, comunícate con el administrador."
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText("Reintentar").performClick()
        assertEquals(true, retried)
    }

    @Test
    fun errorAlGuardar_muestraErrorSinConfirmarCreacion() {
        var returnedToCatalog = false
        setContent(
            AlbumCreateUiState.SaveError,
            onBack = { returnedToCatalog = true },
        )

        composeTestRule.onNodeWithText("No se pudo crear el álbum. Inténtalo de nuevo.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertDoesNotExist()
        composeTestRule.onNodeWithText("Volver al catálogo").performClick()
        assertTrue(returnedToCatalog)
    }

    @Test
    fun clickRegresar_ejecutaCallback() {
        var backClicked = false
        val testAlbum = Album(id = 0, name = "Abbey Road", cover = "", releaseDate = "", genre = "Rock")
        setContent(
            AlbumCreateUiState.Success(album = testAlbum),
            onBack = { backClicked = true },
        )

        composeTestRule.onNodeWithContentDescription("Regresar").performClick()
        assertEquals(true, backClicked)
    }

    @Test
    fun snackbarExito_muestraMensajeYPermiteVerAlbum() {
        var viewAlbumClicked = false
        val testAlbum = Album(id = 0, name = "Abbey Road", cover = "", releaseDate = "", genre = "Rock")
        setContent(
            AlbumCreateUiState.Success(
                album = testAlbum,
                showSnackbar = true,
            ),
            onAlbumSubmit = { viewAlbumClicked = true },
        )

        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertIsDisplayed()
        composeTestRule.onNodeWithText("VER ÁLBUM").performClick()
        assertEquals(true, viewAlbumClicked)
    }

    @Test
    fun guardarFormularioVacio_muestraErroresYSeActualizanAlCorregir() {
        val service = FakeVinilosApiService()
        val viewModel = AlbumCreateViewModel(AlbumRepository(service))
        var submitted = false
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                AlbumCreateScreen(
                    viewModel = viewModel,
                    onAlbumSubmit = { submitted = true },
                    onBack = {},
                    onAlbumSaveFailed = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Selecciona una fecha").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Selecciona un género").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Selecciona un sello discográfico")
            .performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar Álbum").performClick()

        composeTestRule.onNodeWithText("Revisa los campos obligatorios y corrige los errores.")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Este campo es obligatorio.").assertCountEquals(6)
        assertTrue(service.albums.isEmpty())
        assertFalse(submitted)

        composeTestRule.onNodeWithContentDescription("Nombre del álbum")
            .performScrollTo().performTextInput("Álbum 2024")
        composeTestRule.onAllNodesWithText("Este campo es obligatorio.").assertCountEquals(5)
        assertEquals("Álbum 2024", viewModel.album.name)
    }

    @Test
    fun formularioCompleto_permiteGuardarDatosIngresados() {
        val service = FakeVinilosApiService()
        val viewModel = AlbumCreateViewModel(AlbumRepository(service))
        var submitted = false
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                AlbumCreateScreen(
                    viewModel = viewModel,
                    onAlbumSubmit = { submitted = true },
                    onBack = {},
                    onAlbumSaveFailed = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Nombre del álbum")
            .performTextInput("Álbum 2024")
        composeTestRule.onNodeWithContentDescription("URL de la carátula")
            .performScrollTo().performTextInput("https://example.invalid/cover.jpg")
        composeTestRule.onNodeWithContentDescription("Fecha de lanzamiento")
            .performScrollTo().performClick()
        composeTestRule.onAllNodes(hasClickAction() and hasText("15", substring = true))
            .onFirst().performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.onNodeWithText("Folk").performScrollTo().performClick()
        composeTestRule.onNodeWithContentDescription("Sello discográfico")
            .performScrollTo().performClick()
        composeTestRule.onNodeWithText("Sony Music").performClick()
        composeTestRule.onNodeWithContentDescription("Descripción curatorial")
            .performScrollTo().performTextInput("Música clásica, edición 2024.")

        composeTestRule.onNodeWithText("Guardar Álbum").performClick()
        composeTestRule.waitForIdle()

        assertTrue(submitted)
        val saved = service.albums.single()
        assertEquals("Álbum 2024", saved.name)
        assertEquals("https://example.invalid/cover.jpg", saved.cover)
        assertTrue(saved.releaseDate.matches(Regex("[0-9]{4}-[0-9]{2}-15")))
        assertEquals("Folk", saved.genre)
        assertEquals("Sony Music", saved.recordLabel)
        assertEquals("Música clásica, edición 2024.", saved.description)
    }
}
