package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
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
            genre = "Rock Clásico",
            recordLabel = "Apple Records / EMI",
        )

        setContent(AlbumCreateUiState.Success(album = testAlbum))

        composeTestRule.onNodeWithText("Nuevo Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("INFORMACIÓN DEL VINILO").assertIsDisplayed()
        composeTestRule.onNodeWithText("HU07").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()

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
}
