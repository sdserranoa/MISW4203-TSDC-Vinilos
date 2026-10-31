package com.example.misw4203_tsdc_vinilos.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Pruebas de interfaz del componente reutilizable de la barra de navegación inferior. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class VinilosBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun muestraLasTresPestaniasDeNavegacion() {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                VinilosBottomBar(
                    currentTab = VinilosTab.ALBUMS,
                    onTabSelected = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artistas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coleccionistas").assertIsDisplayed()
    }

    @Test
    fun alHacerClicEnCadaPestania_invocaElCallbackConLaPestaniaSeleccionada() {
        var tabSeleccionada: VinilosTab? = null

        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                VinilosBottomBar(
                    currentTab = VinilosTab.ALBUMS,
                    onTabSelected = { tabSeleccionada = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Álbumes").performClick()
        assertEquals(VinilosTab.ALBUMS, tabSeleccionada)

        composeTestRule.onNodeWithText("Artistas").performClick()
        assertEquals(VinilosTab.ARTISTS, tabSeleccionada)

        composeTestRule.onNodeWithText("Coleccionistas").performClick()
        assertEquals(VinilosTab.COLLECTORS, tabSeleccionada)
    }
}
