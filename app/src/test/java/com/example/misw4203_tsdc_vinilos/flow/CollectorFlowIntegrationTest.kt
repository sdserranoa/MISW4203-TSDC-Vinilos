package com.example.misw4203_tsdc_vinilos.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.navigation.VinilosNavHost
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de integración del flujo de coleccionistas (HU05) a través de [VinilosNavHost], en la
 * JVM con Robolectric. Recorren las capas reales (pantalla, ViewModel, Repository y Service
 * Adapter) con sus dispatchers de producción; solo el servicio HTTP es falso.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class CollectorFlowIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeService = FakeVinilosApiService(
        albums = listOf(testAlbum(1, name = "Buscando América")),
        collectors = listOf(
            testCollector(100, "Manolo Bellon", email = "manollo@caracol.com.co", telephone = "3502457896", albumCount = 1),
            testCollector(101, "Jaime Monsalve", email = "jmonsalve@rtvc.com.co", telephone = "3012357936", albumCount = 2),
        ),
    )

    @Before
    fun setUp() {
        VinilosApi.customService = fakeService
    }

    @After
    fun tearDown() {
        VinilosApi.customService = null
    }

    private fun setContent() {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                VinilosNavHost()
            }
        }
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openCollectorsTab() {
        composeTestRule.onNodeWithText("Coleccionistas").performClick()
        waitForText("Manolo Bellon")
    }

    @Test
    fun pestanaColeccionistas_muestraElListadoConNombreCorreoYAlbumes() {
        setContent()

        openCollectorsTab()

        composeTestRule.onNodeWithText("Coleccionistas").assertIsSelected()
        composeTestRule.onNodeWithText("2 coleccionistas registrados").assertIsDisplayed()
        composeTestRule.onNodeWithText("manollo@caracol.com.co").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 álbumes").assertIsDisplayed()
    }

    @Test
    fun filtroPorNombre_muestraSoloLosColeccionistasQueCoinciden() {
        setContent()
        openCollectorsTab()

        composeTestRule.onNodeWithText("Buscar coleccionista por nombre…").performTextInput("Jaime")
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("Manolo Bellon")).fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithText("1 coleccionista registrado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
    }

    @Test
    fun botonDeDetalle_navegaAlDetalleYVolverRegresaAlListado() {
        setContent()
        openCollectorsTab()

        composeTestRule.onNodeWithContentDescription("Ver detalle de Jaime Monsalve").performClick()
        waitForText("3012357936")

        composeTestRule.onNodeWithText("Detalle de Coleccionista").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeTestRule.onNodeWithText("jmonsalve@rtvc.com.co").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coleccionistas").assertIsSelected()

        composeTestRule.onNodeWithContentDescription("Volver a coleccionistas").performClick()
        waitForText("2 coleccionistas registrados")
        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
    }

    @Test
    fun cambiarDePestana_conservaElFiltroDelListadoDeColeccionistas() {
        setContent()
        openCollectorsTab()
        composeTestRule.onNodeWithText("Buscar coleccionista por nombre…").performTextInput("Jaime")
        waitForText("1 coleccionista registrado")

        composeTestRule.onNodeWithText("Álbumes").performClick()
        waitForText("Buscando América")
        composeTestRule.onNodeWithText("Coleccionistas").performClick()

        waitForText("1 coleccionista registrado")
        composeTestRule.onNodeWithText("Jaime").assertIsDisplayed()
    }

    @Test
    fun errorAlCargarColeccionistas_muestraMensajeYReintentarRecuperaElListado() {
        fakeService.error = java.io.IOException("Sin conexión")
        setContent()

        composeTestRule.onNodeWithText("Coleccionistas").performClick()
        val errorMessage =
            "Ocurrió un error al cargar los coleccionistas. Por favor, comunícate con el administrador."
        waitForText(errorMessage)
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        fakeService.error = null
        composeTestRule.onNodeWithText("Reintentar").performClick()

        waitForText("Manolo Bellon")
        composeTestRule.onNodeWithText("2 coleccionistas registrados").assertIsDisplayed()
    }
}
