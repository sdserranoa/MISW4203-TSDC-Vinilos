package com.example.misw4203_tsdc_vinilos.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.misw4203_tsdc_vinilos.data.network.BandDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.MusicianDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.PerformerPrize
import com.example.misw4203_tsdc_vinilos.model.Prize
import com.example.misw4203_tsdc_vinilos.model.Track
import com.example.misw4203_tsdc_vinilos.navigation.VinilosNavHost
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de integración del flujo de usuario End-to-End (E2E):
 * `Catálogo de Álbumes (HU01)` -> `Detalle de Álbum (HU02)` -> `Detalle de Artista (HU04)`.
 *
 * Ejecutadas en la JVM con Robolectric simulando pantallas Android en múltiples versiones de SDK.
 *
 * Valida:
 * 1. Navegación fluida Catálogo -> Álbum -> Intérpretes -> Artista Solista.
 * 2. Visualización e interacción completa en HU04 (Hero, Favoritos, Biografía, Álbumes 180g, Premios, Coleccionistas).
 * 3. Retorno al Detalle de Álbum y al Catálogo general.
 * 4. Navegación fluida hacia una Banda / Agrupación (verificando miembros e insignia de Banda).
 * 5. Navegación cruzada: Clic en un álbum del carrusel del artista regresa al Detalle del Álbum.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class ArtistFlowIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val rubenBladesPerformer = Performer(
        id = 100,
        name = "Rubén Blades Bellido de Luna",
        birthDate = "1948-07-16T00:00:00.000Z",
        description = "Es un cantante, compositor, músico, actor, abogado, político y activista panameño que ha desarrollado gran parte de su exitosa carrera artística en la ciudad de Nueva York.",
    )

    private val pescadoRabiosoBand = Performer(
        id = 200,
        name = "Pescado Rabioso",
        creationDate = "1971-01-01T00:00:00.000Z",
        description = "Legendaria banda de rock argentina fundada por Luis Alberto Spinetta.",
    )

    private val spinettaPerformer = Performer(
        id = 201,
        name = "Luis Alberto Spinetta",
        birthDate = "1950-01-23T00:00:00.000Z",
        description = "Músico, cantante, guitarrista y poeta argentino.",
    )

    private val buscandoAmericaAlbum = testAlbum(
        id = 1,
        name = "Buscando América",
        genre = "Salsa",
        releaseDate = "1984-08-01T00:00:00.000Z",
        recordLabel = "Elektra",
        performers = listOf(rubenBladesPerformer),
        tracks = listOf(Track(1, "Decisiones", "5:05")),
    )

    private val artaudAlbum = testAlbum(
        id = 2,
        name = "Artaud",
        genre = "Rock",
        releaseDate = "1973-01-01T00:00:00.000Z",
        recordLabel = "Talent",
        performers = listOf(pescadoRabiosoBand),
        tracks = listOf(Track(2, "Todas las hojas son del viento", "2:12")),
    )

    private val sampleMusicians = listOf(
        MusicianDetailResponse(
            id = 100,
            name = "Rubén Blades Bellido de Luna",
            image = "",
            description = "Es un cantante, compositor, músico, actor, abogado, político y activista panameño que ha desarrollado gran parte de su exitosa carrera artística en la ciudad de Nueva York.",
            birthDate = "1948-07-16T00:00:00.000Z",
            albums = listOf(buscandoAmericaAlbum),
            performerPrizes = listOf(PerformerPrize(id = 500, premiationDate = "1978-12-10T00:00:00.000Z")),
        )
    )

    private val sampleBands = listOf(
        BandDetailResponse(
            id = 200,
            name = "Pescado Rabioso",
            image = "",
            description = "Legendaria banda de rock argentina fundada por Luis Alberto Spinetta.",
            creationDate = "1971-01-01T00:00:00.000Z",
            albums = listOf(artaudAlbum),
            musicians = listOf(spinettaPerformer),
            performerPrizes = emptyList(),
        )
    )

    private val samplePrizes = listOf(
        Prize(
            id = 500,
            organization = "National Academy of Recording Arts & Sciences",
            name = "Grammy Award",
            description = "Grammy al mejor álbum latino.",
        )
    )

    private val sampleCollectors = listOf(
        Collector(
            id = 10,
            name = "Carlos Melómano",
            favoritePerformers = listOf(rubenBladesPerformer),
        )
    )

    private val fakeService = FakeVinilosApiService(
        albums = listOf(buscandoAmericaAlbum, artaudAlbum),
        musicians = sampleMusicians,
        bands = sampleBands,
        prizes = samplePrizes,
        collectors = sampleCollectors,
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

    @Test
    fun flujoE2E_desdeCatalogoAAlbumYArtistaSolista_conInteraccionesYRetorno() {
        setContent()

        // 1. Paso 1: Catálogo de Álbumes (HU01)
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()

        // 2. Paso 2: Navegar a Detalle de Álbum (HU02)
        composeTestRule.onNodeWithText("Buscando América").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Álbum").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salsa").assertIsDisplayed()

        // 3. Paso 3: Abrir Pestaña de Intérpretes y verificar performer
        composeTestRule.onNodeWithText("Intérpretes").performClick()
        composeTestRule.onNodeWithText("INTÉRPRETES & MÚSICOS").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Rubén Blades Bellido de Luna").onFirst().assertIsDisplayed()

        // 4. Paso 4: Navegar a Detalle del Artista (HU04) haciendo clic en la tarjeta del intérprete
        composeTestRule.onNode(hasText("Rubén Blades Bellido de Luna") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Esperar a que la carga asíncrona del artista finalice
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Artista").fetchSemanticsNodes().isNotEmpty()
        }

        // 5. Verificar elementos de la pantalla de Detalle de Artista (HU04)
        composeTestRule.onNodeWithText("Detalle de Artista").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Rubén Blades Bellido de Luna").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Nacimiento: 1948-07-16").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solista").assertIsDisplayed()

        // Interacción: Botón de Favoritos
        composeTestRule.onNodeWithText("Agregar a favoritos").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("En favoritos").assertIsDisplayed()

        // Interacción: Biografía y Leer más
        composeTestRule.onNodeWithText("Leer más").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Leer menos").performScrollTo().assertIsDisplayed()

        // Sección: Álbumes asociados (Carrusel con badge 180g)
        composeTestRule.onNodeWithText("Álbumes asociados (1)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("180g").performScrollTo().assertIsDisplayed()

        // Sección: Premios & Reconocimientos
        composeTestRule.onNodeWithText("Premios & Reconocimientos").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Grammy Award").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("National Academy of Recording Arts & Sciences").performScrollTo().assertIsDisplayed()

        // Sección: Coleccionistas que lo siguen
        composeTestRule.onNodeWithText("Coleccionistas que lo siguen (1)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Carlos Melómano").assertExists()

        // 6. Paso 5: Regresar con navegación superior al Detalle de Álbum
        composeTestRule.onNodeWithContentDescription("Volver atrás").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Álbum").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()

        // 7. Paso 6: Regresar al Catálogo
        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Vinilos").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 2 álbumes clásicos").assertIsDisplayed()
    }

    @Test
    fun flujoE2E_desdeCatalogoAAlbumYAgrupacionMusical_conMiembrosYRetorno() {
        setContent()

        // 1. Paso 1: Encontrar y abrir álbum "Artaud"
        composeTestRule.onNodeWithText("Artaud").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Álbum").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()

        // 2. Paso 2: Abrir Pestaña de Intérpretes y pulsar sobre la Banda
        composeTestRule.onNodeWithText("Intérpretes").performClick()
        composeTestRule.onNode(hasText("Pescado Rabioso") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Esperar a que la carga asíncrona de la banda finalice
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Artista").fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Paso 3: Verificar ficha de la Banda (HU04)
        composeTestRule.onNodeWithText("Detalle de Artista").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Pescado Rabioso").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Creación: 1971-01-01").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banda").assertIsDisplayed()

        // Sección específica de Banda: Integrantes de la Agrupación
        composeTestRule.onNodeWithText("Integrantes de la agrupación (1)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Luis Alberto Spinetta").performScrollTo().assertIsDisplayed()

        // 4. Paso 4: Retorno ordenado
        composeTestRule.onNodeWithContentDescription("Volver atrás").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Álbum").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artaud").assertIsDisplayed()
    }

    @Test
    fun flujoE2E_carruselDeAlbumesEnArtista_navegaCruzadoADetalleDeAlbum() {
        setContent()

        // Navegar: Catálogo -> Buscando América -> Rubén Blades
        composeTestRule.onNodeWithText("Buscando América").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Álbum").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Intérpretes").performClick()
        composeTestRule.onNode(hasText("Rubén Blades Bellido de Luna") and hasClickAction())
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Artista").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Detalle de Artista").assertIsDisplayed()

        // Clic en el álbum "Buscando América" desde el carrusel de álbumes del artista
        composeTestRule.onNodeWithText("Álbumes asociados (1)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNode(hasText("Buscando América") and hasClickAction())
            .performScrollTo()
            .performClick()

        // Confirma que navegó al Detalle del Álbum
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Detalle de Álbum").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
    }
}
