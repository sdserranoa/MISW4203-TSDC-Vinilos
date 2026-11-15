package com.example.misw4203_tsdc_vinilos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Collector
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Pruebas End-to-End (E2E) instrumentadas con Jetpack Compose UI Test (sobre Espresso) para el
 * listado de coleccionistas (HU05), ejecutadas en dispositivo o emulador Android.
 *
 * Recorren la app desde [MainActivity]: pestaña Coleccionistas de la barra inferior, listado con
 * nombre, correo y cantidad de álbumes, filtro por nombre, paginación en el cliente, navegación al
 * detalle con el botón de cada coleccionista y regreso al listado, además de los mensajes de
 * listado vacío y de error. El servicio HTTP se reemplaza por uno falso en [VinilosApi], así que
 * las pruebas no dependen del backend.
 */
@RunWith(AndroidJUnit4::class)
class CollectorFlowE2ETest {

    companion object {
        private val fakeService = CollectorsFakeService()

        private fun collector(id: Int, name: String, email: String, telephone: String, albums: Int) =
            CollectorsTestData.collector(id, name, email, telephone, albums)

        private val backendCollectors = listOf(
            collector(100, "Manolo Bellon", "manollo@caracol.com.co", "3502457896", albums = 1),
            collector(101, "Jaime Monsalve", "jmonsalve@rtvc.com.co", "3012357936", albums = 1),
            collector(102, "Camila Restrepo", "c.restrepo@musicbox.co", "3104567890", albums = 3),
        )

        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            VinilosApi.customService = fakeService
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            VinilosApi.customService = null
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openCollectorsTab(collectors: List<Collector>, error: Exception? = null) {
        fakeService.reset()
        fakeService.collectors = collectors
        fakeService.collectorsError = error
        composeTestRule.onNodeWithText("Coleccionistas").performClick()
    }

    @Test
    fun flujoCompleto_listadoConFiltro_navegacionAlDetalle_yRetornoAlListado() {
        // 1. Abrir la pestaña Coleccionistas desde la barra inferior
        openCollectorsTab(backendCollectors)
        waitForText("Manolo Bellon")

        // 2. Listado: encabezado, nombre, correo y cantidad de álbumes (criterio 2)
        composeTestRule.onNodeWithText("Coleccionistas").assertIsSelected()
        composeTestRule.onNodeWithText("3 coleccionistas registrados").assertIsDisplayed()
        composeTestRule.onNodeWithText("manollo@caracol.com.co").assertIsDisplayed()
        composeTestRule.onNodeWithText("Camila Restrepo").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 álbumes").assertIsDisplayed()

        // 3. Filtrar por nombre (criterio 4)
        composeTestRule.onNodeWithText("Buscar por nombre…").performTextInput("Jaime")
        waitForText("1 coleccionista registrado")
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manolo Bellon").assertDoesNotExist()

        // 4. Limpiar el filtro
        composeTestRule.onNodeWithContentDescription("Limpiar búsqueda de coleccionistas").performClick()
        waitForText("3 coleccionistas registrados")
        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()

        // 5. Navegar al detalle con el botón del coleccionista (criterio 3)
        composeTestRule.onNodeWithContentDescription("Ver detalle de Jaime Monsalve").performClick()
        waitForText("3012357936")
        composeTestRule.onNodeWithText("Detalle de Coleccionista").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeTestRule.onNodeWithText("jmonsalve@rtvc.com.co").assertIsDisplayed()

        // 6. Regresar al listado
        composeTestRule.onNodeWithContentDescription("Volver a coleccionistas").performClick()
        waitForText("3 coleccionistas registrados")
        composeTestRule.onNodeWithText("Camila Restrepo").assertIsDisplayed()
    }

    @Test
    fun listado_conMasDeDiezColeccionistas_paginaEnElClienteEnAmbasDirecciones() {
        val collectors = (1..15).map { collector(it, "Coleccionista $it", "c$it@vinilos.co", "30000000$it", 1) }
        openCollectorsTab(collectors)
        waitForText("15 coleccionistas registrados")

        // Página 1: del 1 al 10
        composeTestRule.onNodeWithText("Coleccionista 1").assertIsDisplayed()
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Pág. 1 de 2"))
        composeTestRule.onNodeWithText("Coleccionista 11").assertDoesNotExist()

        // Página 2: del 11 al 15
        composeTestRule.onNodeWithContentDescription("Página siguiente").performClick()
        waitForText("Coleccionista 11")
        composeTestRule.onNodeWithText("Coleccionista 1").assertDoesNotExist()
        // En pantallas bajas (p. ej. Nexus 6P) el último coleccionista queda fuera del área visible
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Coleccionista 15"))
        composeTestRule.onNodeWithText("Coleccionista 15").assertIsDisplayed()

        // Regreso a la página 1
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Pág. 2 de 2"))
        composeTestRule.onNodeWithContentDescription("Página anterior").performClick()
        waitForText("Coleccionista 1")
        composeTestRule.onNodeWithText("Coleccionista 11").assertDoesNotExist()
    }

    @Test
    fun listado_sinColeccionistasRegistrados_muestraMensajeVacio() {
        openCollectorsTab(emptyList())

        waitForText("No hay coleccionistas registrados.")
        composeTestRule.onNodeWithText("No hay coleccionistas registrados.").assertIsDisplayed()
    }

    @Test
    fun listado_conErrorDelServicio_muestraMensajeYReintentarRecuperaElListado() {
        val errorMessage =
            "Ocurrió un error al cargar los coleccionistas. Por favor, comunícate con el administrador."
        openCollectorsTab(backendCollectors, error = IOException("Sin conexión"))
        waitForText(errorMessage)
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        fakeService.collectorsError = null
        composeTestRule.onNodeWithText("Reintentar").performClick()

        waitForText("Manolo Bellon")
        composeTestRule.onNodeWithText("3 coleccionistas registrados").assertIsDisplayed()
    }
}
