package com.example.misw4203_tsdc_vinilos.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Pruebas de integración del contenedor principal con la barra de navegación inferior. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class VinilosNavHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun alIniciar_muestraLaBarraDeNavegacionInferiorYElCatalogoDeAlbumes() {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                VinilosNavHost()
            }
        }

        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artistas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coleccionistas").assertIsDisplayed()
    }

    @Test
    fun alHacerClicEnAlbumes_permaneceEnElCatalogoDeAlbumes() {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                VinilosNavHost()
            }
        }

        composeTestRule.onNodeWithText("Álbumes").performClick()
        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()
    }
}
