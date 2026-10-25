package com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de interfaz en Jetpack Compose del detalle de un álbum (HU02),
 * ejecutadas en la JVM con Robolectric sobre una pantalla de 411 x 891 dp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class AlbumDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: AlbumDetailUiState,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                AlbumDetailContent(
                    uiState = uiState,
                    onBack = onBack,
                    onRetry = onRetry,
                )
            }
        }
    }

    @Test
    fun exito_muestraTituloArtistasGeneroAnioDiscograficaYDescripcion() {
        val album = testAlbum(
            id = 100,
            name = "Abbey Road",
            genre = "Rock Clásico",
            releaseDate = "1969-09-26T00:00:00.000Z",
            description = "Álbum icónico grabado en los estudios EMI.",
            recordLabel = "Apple Records",
            performers = listOf(Performer(1, "The Beatles")),
        )

        setContent(AlbumDetailUiState.Success(album))

        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()
        composeTestRule.onNodeWithText("The Beatles").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rock Clásico").assertIsDisplayed()
        composeTestRule.onNodeWithText("1969").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apple Records").assertIsDisplayed()
        composeTestRule.onNodeWithText("180g Audiophile Vinyl").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum icónico grabado en los estudios EMI.").assertIsDisplayed()
    }

    @Test
    fun exito_clicEnVolver_ejecutaCallback() {
        var backClicked = false
        val album = testAlbum(id = 100, name = "Abbey Road")

        setContent(
            uiState = AlbumDetailUiState.Success(album),
            onBack = { backClicked = true },
        )

        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun loading_muestraMensajeDeCarga() {
        setContent(AlbumDetailUiState.Loading)
        composeTestRule.onNodeWithText("Cargando detalle del álbum…").assertIsDisplayed()
    }

    @Test
    fun error_muestraMensajeYPermiteReintentar() {
        var retried = false
        setContent(
            uiState = AlbumDetailUiState.Error,
            onRetry = { retried = true },
        )

        composeTestRule.onNode(hasText("Ocurrió un error al cargar el álbum", substring = true)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()
        assertTrue(retried)
    }

    @Test
    fun descripcionLarga_permiteExpandirYColapsar() {
        val longDescription = "Esta es una descripción muy detallada que supera los ciento cuarenta caracteres requeridos para desplegar el botón interactivo de ver más detalles sobre la obra musical y su contexto histórico."
        val album = testAlbum(
            id = 100,
            name = "Álbum con descripción larga",
            description = longDescription,
        )

        setContent(AlbumDetailUiState.Success(album))

        composeTestRule.onNodeWithText("Leer más").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leer más").performClick()
        composeTestRule.onNodeWithText("Leer menos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leer menos").performClick()
        composeTestRule.onNodeWithText("Leer más").assertIsDisplayed()
    }
}
