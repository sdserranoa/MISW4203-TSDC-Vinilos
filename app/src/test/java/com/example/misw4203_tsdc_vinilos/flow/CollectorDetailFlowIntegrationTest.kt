package com.example.misw4203_tsdc_vinilos.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.misw4203_tsdc_vinilos.data.network.MusicianDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.navigation.VinilosNavHost
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import com.example.misw4203_tsdc_vinilos.testutil.testCollectorAlbum
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de integración del detalle de coleccionista (HU06) a través de [VinilosNavHost], en la
 * JVM con Robolectric: listado → detalle → listados del coleccionista → detalle de un álbum, con
 * las capas reales y sus dispatchers de producción. Solo el servicio HTTP es falso.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class CollectorDetailFlowIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val buscandoAmerica = testAlbum(
        id = 100,
        name = "Buscando América",
        genre = "Salsa",
        recordLabel = "Elektra",
        releaseDate = "1984-08-01T00:00:00.000Z",
    )

    private val fakeService = FakeVinilosApiService(
        albums = listOf(buscandoAmerica),
        collectors = listOf(
            testCollector(
                id = 100,
                name = "Manolo Bellon",
                email = "manollo@caracol.com.co",
                telephone = "3502457896",
                favoritePerformers = listOf(Performer(100, "Rubén Blades Bellido de Luna")),
                comments = listOf(Comment(100, "The most relevant album of Ruben Blades", 5)),
            ),
            testCollector(101, "Jaime Monsalve", albumCount = 0),
        ),
        collectorAlbums = mapOf(100 to listOf(testCollectorAlbum(buscandoAmerica))),
        musicians = listOf(
            MusicianDetailResponse(
                id = 100,
                name = "Rubén Blades Bellido de Luna",
                description = "Cantante, compositor, músico, actor, abogado, político y activista panameño.",
                birthDate = "1948-07-16T00:00:00.000Z",
            ),
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

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openCollectorDetail(name: String) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                VinilosNavHost()
            }
        }
        composeTestRule.onNodeWithText("Coleccionistas").performClick()
        waitForText(name)
        composeTestRule.onNodeWithContentDescription("Ver detalle de $name").performClick()
        waitForText("Detalle de Coleccionista")
    }

    @Test
    fun detalle_muestraContactoYAlbumesDelColeccionista() {
        openCollectorDetail("Manolo Bellon")
        waitForText("Álbumes (1)")

        composeTestRule.onNodeWithText("3502457896").assertIsDisplayed()
        composeTestRule.onNodeWithText("manollo@caracol.com.co").assertIsDisplayed()
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Buscando América"))
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
    }

    @Test
    fun album_navegaAlDetalleDelAlbumSinSalirDeLaSeccionColeccionistas() {
        openCollectorDetail("Manolo Bellon")
        waitForText("Álbumes (1)")
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Buscando América"))

        composeTestRule.onNodeWithText("Buscando América").performClick()
        waitForText("Detalle de Álbum")

        composeTestRule.onNodeWithText("Elektra").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coleccionistas").assertIsSelected()

        // Volver regresa al detalle del coleccionista, no al catálogo
        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()
        waitForText("Detalle de Coleccionista")
        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
    }

    @Test
    fun pestanaColeccionistas_desdeElDetalleDeUnAlbum_regresaAlListado() {
        openCollectorDetail("Manolo Bellon")
        waitForText("Álbumes (1)")
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Buscando América"))
        composeTestRule.onNodeWithText("Buscando América").performClick()
        waitForText("Detalle de Álbum")

        composeTestRule.onNodeWithText("Coleccionistas").performClick()

        waitForText("2 coleccionistas registrados")
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
    }

    @Test
    fun pestanasArtistasYComentarios_muestranLosRegistrosDelColeccionista() {
        openCollectorDetail("Manolo Bellon")
        waitForText("Artistas (1)")

        composeTestRule.onNodeWithText("Artistas (1)").performClick()
        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("Rubén Blades Bellido de Luna"))
        composeTestRule.onNodeWithText("Rubén Blades Bellido de Luna").assertIsDisplayed()

        composeTestRule.onNodeWithText("Comentarios (1)").performClick()
        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("The most relevant album of Ruben Blades"))
        composeTestRule.onNodeWithText("The most relevant album of Ruben Blades").assertIsDisplayed()
    }

    @Test
    fun artistaFavorito_navegaAlDetalleDelArtistaYVolverRegresaAlColeccionista() {
        openCollectorDetail("Manolo Bellon")
        waitForText("Artistas (1)")
        composeTestRule.onNodeWithText("Artistas (1)").performClick()
        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("Rubén Blades Bellido de Luna"))

        composeTestRule.onNodeWithText("Rubén Blades Bellido de Luna").performClick()
        waitForText("Detalle de Artista")

        composeTestRule.onAllNodes(hasText("Rubén Blades Bellido de Luna")).onFirst().assertExists()
        composeTestRule.onNodeWithText("Coleccionistas").assertIsSelected()

        composeTestRule.onNodeWithContentDescription("Volver atrás").performClick()
        waitForText("Detalle de Coleccionista")
        composeTestRule.onNodeWithText("Artistas (1)").assertIsSelected()
    }

    @Test
    fun coleccionistaSinRegistros_muestraMensajesDeNoHayRegistros() {
        openCollectorDetail("Jaime Monsalve")
        waitForText("Álbumes (0)")

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("No hay álbumes registrados para este coleccionista."))
        composeTestRule.onNodeWithText("No hay álbumes registrados para este coleccionista.").assertIsDisplayed()

        composeTestRule.onNodeWithText("Artistas (0)").performClick()
        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("No hay artistas favoritos registrados para este coleccionista."))
        composeTestRule.onNodeWithText("No hay artistas favoritos registrados para este coleccionista.")
            .assertIsDisplayed()
    }
}
