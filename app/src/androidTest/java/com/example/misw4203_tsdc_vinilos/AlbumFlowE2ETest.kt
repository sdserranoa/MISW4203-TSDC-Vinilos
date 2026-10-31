package com.example.misw4203_tsdc_vinilos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApiService
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.CommentCollector
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.Track
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas End-to-End (E2E) instrumentadas con Jetpack Compose Test ejecutadas en dispositivo / emulador Android.
 *
 * Valida el recorrido de usuario completo desde el inicio de la aplicación en [MainActivity],
 * visualización del catálogo (HU01), filtrado de búsqueda, navegación hacia el detalle del álbum (HU02),
 * interacción con las pestañas de contenido (Canciones, Intérpretes, Comentarios)
 * y regreso al catálogo general.
 */
@RunWith(AndroidJUnit4::class)
class AlbumFlowE2ETest {

    companion object {
        private val testAlbums = listOf(
            Album(
                id = 100,
                name = "Buscando América",
                cover = "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Buscando América es el primer álbum de la banda de Rubén Blades y Seis del Solar.",
                genre = "Salsa",
                recordLabel = "Elektra",
                performers = listOf(
                    Performer(
                        id = 100,
                        name = "Rubén Blades Bellido de Luna",
                        birthDate = "1948-07-16T00:00:00.000Z",
                        description = "Es un cantante, compositor, músico, actor, abogado, político y activista panameño.",
                    )
                ),
                tracks = listOf(
                    Track(100, "Decisiones", "5:05"),
                    Track(101, "Desapariciones", "6:29"),
                ),
                comments = listOf(
                    Comment(
                        id = 100,
                        description = "The most relevant album of Ruben Blades",
                        rating = 5,
                        collector = CommentCollector(1, "Carlos Melómano"),
                    )
                ),
            ),
            Album(
                id = 101,
                name = "Poeta del pueblo",
                cover = "",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Recopilación de 27 composiciones del cosmos Blades.",
                genre = "Salsa",
                recordLabel = "Elektra",
                performers = listOf(
                    Performer(id = 100, name = "Rubén Blades Bellido de Luna")
                ),
                tracks = emptyList(),
                comments = emptyList(),
            ),
            Album(
                id = 102,
                name = "A Night at the Opera",
                cover = "",
                releaseDate = "1975-11-21T00:00:00.000Z",
                description = "El cuarto álbum de estudio de la banda británica Queen.",
                genre = "Rock",
                recordLabel = "EMI",
                performers = listOf(
                    Performer(
                        id = 101,
                        name = "Queen",
                        creationDate = "1970-01-01T00:00:00.000Z",
                        description = "Queen es una legendaria banda británica de rock.",
                    )
                ),
                tracks = emptyList(),
                comments = emptyList(),
            ),
        )

        private val fakeService = object : VinilosApiService {
            override suspend fun getAlbums(): List<Album> = testAlbums
            override suspend fun getAlbum(albumId: Int): Album =
                testAlbums.firstOrNull { it.id == albumId }
                    ?: throw NoSuchElementException("Album $albumId not found")
        }

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

    @Test
    fun flujoCompleto_catalogoConFiltro_navegacionADetallePestanas_yRetornoAlCatalogo() {
        // 1. Esperar a que el catálogo cargue los álbumes
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("Buscando América").fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Verificar elementos del Catálogo (HU01) y Barra inferior
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbumes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Artistas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coleccionistas").assertIsDisplayed()

        // 3. Filtrar por término
        composeTestRule.onNodeWithText("Buscar por nombre, género o artista…")
            .performTextInput("Queen")
        composeTestRule.onNodeWithText("A Night at the Opera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertDoesNotExist()

        // 4. Limpiar búsqueda
        composeTestRule.onNodeWithContentDescription("Limpiar búsqueda").performClick()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()

        // 5. Seleccionar álbum para navegar al detalle
        composeTestRule.onNodeWithText("Buscando América").performClick()

        // 6. Verificar encabezado y datos principales del detalle
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salsa").assertIsDisplayed()
        composeTestRule.onNodeWithText("1984").assertIsDisplayed()
        composeTestRule.onNodeWithText("Elektra").assertIsDisplayed()

        // 7. Verificar Pestaña 1: Canciones
        composeTestRule.onNodeWithText("Canciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("PISTAS DEL ÁLBUM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Decisiones").assertIsDisplayed()
        composeTestRule.onNodeWithText("5:05").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desapariciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("6:29").assertIsDisplayed()

        // 8. Cambiar a Pestaña 2: Intérpretes y verificar artista
        composeTestRule.onNodeWithText("Intérpretes").performClick()
        composeTestRule.onNodeWithText("INTÉRPRETES & MÚSICOS").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Rubén Blades Bellido de Luna").onFirst().performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Músico / Solista").performScrollTo().assertIsDisplayed()

        // 9. Cambiar a Pestaña 3: Comentarios y verificar reseña
        composeTestRule.onNodeWithText("Comentarios").performClick()
        composeTestRule.onNodeWithText("RESEÑAS DESTACADAS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Carlos Melómano").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("✓ Coleccionista Verificado").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("\"The most relevant album of Ruben Blades\"").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("★★★★★").performScrollTo().assertIsDisplayed()

        // 10. Regresar al catálogo mediante el botón de navegación superior
        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()

        // 11. Verificar que estamos de vuelta en el catálogo
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()
    }

    @Test
    fun flujoDetalle_albumSinPistas_muestraMensajeVacio() {
        // 1. Esperar al catálogo
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("Poeta del pueblo").fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Navegar a álbum sin canciones
        composeTestRule.onNodeWithText("Poeta del pueblo").performClick()

        // 3. Verificar estado vacío en pistas
        composeTestRule.onNodeWithText("Este álbum aún no tiene pistas registradas en el catálogo.")
            .performScrollTo()
            .assertIsDisplayed()

        // 4. Regresar
        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
    }
}
