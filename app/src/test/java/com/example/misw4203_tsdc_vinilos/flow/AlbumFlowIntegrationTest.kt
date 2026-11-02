package com.example.misw4203_tsdc_vinilos.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.CommentCollector
import com.example.misw4203_tsdc_vinilos.model.Performer
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
 * Pruebas de integración del flujo completo de usuario (HU01 - Catálogo y HU02 - Detalle),
 * ejecutadas en la JVM con Robolectric sobre una pantalla de teléfono de 411 x 891 dp
 * a través de múltiples versiones de Android SDK ([23, 26, 34, 35, 36]).
 *
 * Cubre:
 * 1. Carga inicial del catálogo con barra de navegación inferior (`Álbumes`, `Artistas`, `Coleccionistas`).
 * 2. Filtrado interactivo en la barra de búsqueda y restauración con botón de limpieza.
 * 3. Navegación hacia el detalle de un álbum seleccionado (`HU01` -> `HU02`).
 * 4. Inspección de metadatos del detalle (título, chips de género, año y disquera).
 * 5. Navegación por las tres pestañas de contenido (`Canciones`, `Intérpretes`, `Comentarios`).
 * 6. Retorno al catálogo general mediante el botón de navegación superior.
 * 7. Flujos alternos con estados vacíos y consultas sin resultados.
 */
 
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class AlbumFlowIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleAlbums = listOf(
        testAlbum(
            id = 1,
            name = "Buscando América",
            genre = "Salsa",
            releaseDate = "1984-08-01T00:00:00.000Z",
            recordLabel = "Elektra",
            description = "Buscando América es el primer álbum de la banda de Rubén Blades y Seis del Solar.",
            performers = listOf(
                Performer(
                    id = 100,
                    name = "Rubén Blades Bellido de Luna",
                    birthDate = "1948-07-16T00:00:00.000Z",
                    description = "Es un cantante, compositor, músico y activista panameño.",
                )
            ),
            tracks = listOf(
                Track(1, "Decisiones", "5:05"),
                Track(2, "Desapariciones", "6:29"),
            ),
            comments = listOf(
                Comment(
                    id = 1,
                    description = "Gran clásico de la salsa con contenido social.",
                    rating = 5,
                    collector = CommentCollector(1, "Carlos Melómano"),
                )
            ),
        ),
        testAlbum(
            id = 2,
            name = "Artaud",
            genre = "Rock",
            releaseDate = "1973-01-01T00:00:00.000Z",
            recordLabel = "Talent",
            performers = listOf(
                Performer(
                    id = 101,
                    name = "Pescado Rabioso",
                    creationDate = "1971-01-01T00:00:00.000Z",
                    description = "Banda de rock argentina liderada por Luis Alberto Spinetta.",
                )
            ),
            tracks = listOf(
                Track(3, "Todas las hojas son del viento", "2:12"),
            ),
            comments = emptyList(),
        ),
        testAlbum(
            id = 3,
            name = "A Night at the Opera",
            genre = "Rock",
            releaseDate = "1975-11-21T00:00:00.000Z",
            recordLabel = "EMI",
            performers = emptyList(),
            tracks = emptyList(),
            comments = emptyList(),
        )
    )

    private val fakeService = FakeVinilosApiService(albums = sampleAlbums)

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
    fun flujoCompleto_catalogoConFiltro_navegacionADetallePestanas_yRetornoAlCatalogo() {
        setContent()

        // 1. Verificación del Catálogo inicial (HU01) y Barra inferior
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artaud").assertIsDisplayed()
        composeTestRule.onNodeWithText("A Night at the Opera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artistas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coleccionistas").assertIsDisplayed()

        // 2. Filtro interactivo de búsqueda
        composeTestRule.onNodeWithText("Buscar por nombre, género o artista…")
            .performTextInput("Salsa")
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artaud").assertDoesNotExist()
        composeTestRule.onNodeWithText("A Night at the Opera").assertDoesNotExist()

        // 3. Limpiar filtro y verificar restauración del catálogo
        composeTestRule.onNodeWithContentDescription("Limpiar búsqueda").performClick()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artaud").assertIsDisplayed()
        composeTestRule.onNodeWithText("A Night at the Opera").assertIsDisplayed()

        // 4. Navegación hacia el Detalle de Álbum (HU01 -> HU02)
        composeTestRule.onNodeWithText("Buscando América").performClick()

        // 5. Verificación de información principal en el Detalle
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salsa").assertIsDisplayed()
        composeTestRule.onNodeWithText("1984").assertIsDisplayed()
        composeTestRule.onNodeWithText("Elektra").assertIsDisplayed()

        // 6. Pestaña 1: Canciones
        composeTestRule.onNodeWithText("Canciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("Decisiones").assertIsDisplayed()
        composeTestRule.onNodeWithText("5:05").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desapariciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("6:29").assertIsDisplayed()

        // 7. Pestaña 2: Intérpretes
        composeTestRule.onNodeWithText("Intérpretes").performClick()
        composeTestRule.onAllNodesWithText("Rubén Blades Bellido de Luna").onFirst().performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Músico / Solista").performScrollTo().assertIsDisplayed()

        // 8. Pestaña 3: Comentarios y Reseñas
        composeTestRule.onNodeWithText("Comentarios").performClick()
        composeTestRule.onNodeWithText("Carlos Melómano").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("✓ Coleccionista Verificado").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("★★★★★").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("\"Gran clásico de la salsa con contenido social.\"").performScrollTo().assertIsDisplayed()

        // 9. Regreso al catálogo mediante botón superior
        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()

        // 10. Verificación de retorno exitoso al catálogo
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()
    }

    @Test
    fun flujoCompleto_albumSinPistasNiComentarios_muestraEstadosVacios() {
        setContent()

        // Navegar al álbum sin canciones ni comentarios
        composeTestRule.onNodeWithText("A Night at the Opera").performClick()
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()

        // Pestaña Canciones: estado vacío
        composeTestRule.onNodeWithText("Canciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("Este álbum aún no tiene pistas registradas en el catálogo.")
            .performScrollTo()
            .assertIsDisplayed()

        // Pestaña Comentarios: estado vacío
        composeTestRule.onNodeWithText("Comentarios").performClick()
        composeTestRule.onNodeWithText("Aún no hay comentarios ni reseñas para este álbum.")
            .performScrollTo()
            .assertIsDisplayed()

        // Regresar al catálogo
        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
    }

    @Test
    fun flujoCompleto_filtroSinCoincidencias_muestraMensajeVacioYRecuperaListado() {
        setContent()

        // Búsqueda sin coincidencias
        composeTestRule.onNodeWithText("Buscar por nombre, género o artista…")
            .performTextInput("Jazz Inexistente")
        composeTestRule.onNodeWithText("No se encontraron álbumes para \"Jazz Inexistente\".")
            .assertIsDisplayed()

        // Limpiar búsqueda restaura el listado original
        composeTestRule.onNodeWithContentDescription("Limpiar búsqueda").performClick()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artaud").assertIsDisplayed()
    }

    @Test
    fun flujoCompleto_barraDeNavegacionInferior_mantieneInteraccionEstable() {
        setContent()

        // Al pulsar Álbumes, se mantiene en el catálogo
        composeTestRule.onNodeWithText("Álbumes").performClick()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()

        // Al pulsar Artistas o Coleccionistas, la UI permanece estable
        composeTestRule.onNodeWithText("Artistas").performClick()
        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()

        composeTestRule.onNodeWithText("Coleccionistas").performClick()
        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()
    }
}
