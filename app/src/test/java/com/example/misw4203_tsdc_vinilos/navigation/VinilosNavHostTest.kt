package com.example.misw4203_tsdc_vinilos.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.testAlbums
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.After
import org.junit.Before
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

    @Before
    fun setUp() {
        VinilosApi.customService = FakeVinilosApiService(albums = testAlbums(2))
    }

    @After
    fun tearDown() {
        VinilosApi.customService = null
    }

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

    @Test
    fun alHacerClicEnAgregarAlbum_navegaAlFormularioDeCreacion() {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                VinilosNavHost()
            }
        }

        composeTestRule.onNodeWithContentDescription("Agregar álbum").performClick()

        composeTestRule.onNodeWithText("Nuevo Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guardar Álbum").assertIsDisplayed()
    }
}
