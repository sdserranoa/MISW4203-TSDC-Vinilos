package com.example.misw4203_tsdc_vinilos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.misw4203_tsdc_vinilos.CollectorsTestData.buscandoAmerica
import com.example.misw4203_tsdc_vinilos.CollectorsTestData.nightAtTheOpera
import com.example.misw4203_tsdc_vinilos.CollectorsTestData.ownedAlbum
import com.example.misw4203_tsdc_vinilos.CollectorsTestData.queen
import com.example.misw4203_tsdc_vinilos.CollectorsTestData.rubenBlades
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Comment
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Pruebas End-to-End (E2E) instrumentadas con Jetpack Compose UI Test (sobre Espresso) para el
 * detalle de coleccionista (HU06), ejecutadas en dispositivo o emulador Android.
 *
 * Recorren la app desde [MainActivity]: pestaña Coleccionistas, detalle de un coleccionista con su
 * teléfono, correo, álbumes, artistas favoritos y comentarios, navegación al detalle de un álbum y
 * regreso, además de los mensajes de listados vacíos y de error. El servicio HTTP se reemplaza por
 * uno falso en [VinilosApi], así que las pruebas no dependen del backend.
 */
@RunWith(AndroidJUnit4::class)
class CollectorDetailE2ETest {

    companion object {
        private val fakeService = CollectorsFakeService()

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

    @Before
    fun setUp() {
        fakeService.reset()
        fakeService.albums = listOf(buscandoAmerica, nightAtTheOpera)
        fakeService.collectors = listOf(
            CollectorsTestData.collector(
                id = 100,
                name = "Manolo Bellon",
                email = "manollo@caracol.com.co",
                telephone = "3502457896",
                albums = 2,
                favoritePerformers = listOf(rubenBlades, queen),
                comments = listOf(Comment(100, "The most relevant album of Ruben Blades", 5)),
            ),
            CollectorsTestData.collector(
                id = 101,
                name = "Jaime Monsalve",
                email = "jmonsalve@rtvc.com.co",
                telephone = "3012357936",
                albums = 0,
            ),
        )
        fakeService.collectorAlbums = mapOf(100 to listOf(ownedAlbum(buscandoAmerica), ownedAlbum(nightAtTheOpera)))
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 8_000) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun scrollTo(text: String) {
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))
    }

    private fun openCollectorDetail(name: String) {
        composeTestRule.onNodeWithText("Coleccionistas").performClick()
        waitForText(name)
        composeTestRule.onNodeWithContentDescription("Ver detalle de $name").performClick()
        waitForText("Detalle de Coleccionista")
    }

    @Test
    fun flujoCompleto_detalleConAlbumesArtistasYComentarios_navegacionAlAlbum_yRetorno() {
        // 1. Abrir el detalle desde el listado de coleccionistas
        openCollectorDetail("Manolo Bellon")
        waitForText("Álbumes (2)")

        // 2. Nombre, teléfono y correo (criterio 1)
        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithText("3502457896").assertIsDisplayed()
        composeTestRule.onNodeWithText("manollo@caracol.com.co").assertIsDisplayed()

        // 3. Álbumes con nombre, género y año (criterio 2)
        composeTestRule.onNodeWithText("Álbumes (2)").assertIsSelected()
        scrollTo("Buscando América")
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("1984").assertIsDisplayed()
        scrollTo("A Night at the Opera")
        composeTestRule.onNodeWithText("A Night at the Opera").assertIsDisplayed()

        // 4. Artistas favoritos con nombre y tipo (criterio 2)
        scrollTo("Artistas (2)")
        composeTestRule.onNodeWithText("Artistas (2)").performClick()
        scrollTo("Rubén Blades Bellido de Luna")
        composeTestRule.onNodeWithText("Rubén Blades Bellido de Luna").assertIsDisplayed()
        scrollTo("Queen")
        composeTestRule.onNodeWithText("Banda / Agrupación").assertIsDisplayed()

        // 5. Comentarios con su calificación (criterio 1)
        scrollTo("Comentarios (1)")
        composeTestRule.onNodeWithText("Comentarios (1)").performClick()
        scrollTo("The most relevant album of Ruben Blades")
        composeTestRule.onNodeWithText("The most relevant album of Ruben Blades").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Calificación: 5 de 5").assertIsDisplayed()

        // 6. Navegar al detalle de un álbum de la colección (criterio 3)
        scrollTo("Álbumes (2)")
        composeTestRule.onNodeWithText("Álbumes (2)").performClick()
        scrollTo("A Night at the Opera")
        composeTestRule.onNodeWithText("A Night at the Opera").performClick()
        waitForText("Detalle de Álbum")
        // El detalle del álbum muestra el sello en más de un lugar
        composeTestRule.onAllNodesWithText("EMI").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Coleccionistas").assertIsSelected()

        // 7. Volver al detalle del coleccionista y luego al listado
        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()
        waitForText("Detalle de Coleccionista")
        // Se conservan la pestaña y la posición del listado desde donde se abrió el álbum
        composeTestRule.onNodeWithText("A Night at the Opera").assertIsDisplayed()
        scrollTo("Manolo Bellon")
        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Volver a coleccionistas").performClick()
        waitForText("2 coleccionistas registrados")
    }

    @Test
    fun coleccionistaSinRegistros_muestraMensajesDeNoHayRegistrosEnCadaListado() {
        openCollectorDetail("Jaime Monsalve")
        waitForText("Álbumes (0)")

        scrollTo("No hay álbumes registrados para este coleccionista.")
        composeTestRule.onNodeWithText("No hay álbumes registrados para este coleccionista.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Artistas (0)").performClick()
        scrollTo("No hay artistas favoritos registrados para este coleccionista.")
        composeTestRule.onNodeWithText("No hay artistas favoritos registrados para este coleccionista.")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Comentarios (0)").performClick()
        scrollTo("No hay comentarios registrados para este coleccionista.")
        composeTestRule.onNodeWithText("No hay comentarios registrados para este coleccionista.")
            .assertIsDisplayed()
    }

    @Test
    fun errorAlCargarElDetalle_muestraMensajeYReintentarRecuperaLaInformacion() {
        val errorMessage =
            "Ocurrió un error al cargar el coleccionista. Por favor, comunícate con el administrador."
        composeTestRule.onNodeWithText("Coleccionistas").performClick()
        waitForText("Manolo Bellon")

        fakeService.collectorsError = IOException("Sin conexión")
        composeTestRule.onNodeWithContentDescription("Ver detalle de Manolo Bellon").performClick()
        waitForText(errorMessage)
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        fakeService.collectorsError = null
        composeTestRule.onNodeWithText("Reintentar").performClick()
        waitForText("Álbumes (2)")
        composeTestRule.onNodeWithText("3502457896").assertIsDisplayed()
    }
}
