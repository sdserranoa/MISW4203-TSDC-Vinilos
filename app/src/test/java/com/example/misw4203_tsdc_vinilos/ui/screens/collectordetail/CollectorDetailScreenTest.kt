package com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Pruebas de interfaz en Jetpack Compose del detalle de un coleccionista. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class CollectorDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: CollectorDetailUiState,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                CollectorDetailContent(uiState = uiState, onBack = onBack, onRetry = onRetry)
            }
        }
    }

    private val manolo: Collector = testCollector(
        id = 100,
        name = "Manolo Bellon",
        email = "manollo@caracol.com.co",
        telephone = "3502457896",
        albumCount = 2,
    )

    @Test
    fun exito_muestraNombreTelefonoYCorreoDelColeccionista() {
        setContent(CollectorDetailUiState.Success(manolo))

        composeTestRule.onNodeWithText("Detalle de Coleccionista").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithText("MB").assertIsDisplayed()
        composeTestRule.onNodeWithText("3502457896").assertIsDisplayed()
        composeTestRule.onNodeWithText("manollo@caracol.com.co").assertIsDisplayed()
    }

    @Test
    fun exito_muestraLosTotalesDeAlbumesFavoritosYComentarios() {
        setContent(CollectorDetailUiState.Success(manolo))

        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Favoritos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Comentarios").assertIsDisplayed()
    }

    @Test
    fun botonVolver_invocaLaNavegacionHaciaAtras() {
        var backClicks = 0
        setContent(CollectorDetailUiState.Success(manolo), onBack = { backClicks++ })

        composeTestRule.onNodeWithContentDescription("Volver a coleccionistas").performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun cargando_muestraIndicadorDeCarga() {
        setContent(CollectorDetailUiState.Loading)

        composeTestRule.onNodeWithText("Cargando información del coleccionista…").assertIsDisplayed()
    }

    @Test
    fun error_muestraMensajeDeContactoConElAdministradorYPermiteReintentar() {
        var retries = 0
        setContent(CollectorDetailUiState.Error, onRetry = { retries++ })

        composeTestRule.onNodeWithText(
            "Ocurrió un error al cargar el coleccionista. Por favor, comunícate con el administrador."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()

        assertEquals(1, retries)
    }
}
