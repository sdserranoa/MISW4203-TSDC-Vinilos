package com.example.misw4203_tsdc_vinilos

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Performer
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Pruebas End-to-End (E2E) instrumentadas de la creación de álbumes (HU07), ejecutadas en
 * dispositivo / emulador Android desde [MainActivity].
 *
 * Cada escenario inicia en el catálogo y abre el formulario con el botón "Agregar álbum":
 * 1. Creación exitosa diligenciando todos los campos (calendario y selectores), confirmación al
 *    usuario, catálogo actualizado con el nuevo álbum y acceso a su detalle.
 * 2. Formulario incompleto: no se crea el álbum y se señalan los campos faltantes.
 * 3. Cancelación con el botón "Cancelar" y con el botón atrás del sistema: no se crea y se notifica.
 * 4. Error del servidor: no se guarda, se redirige al catálogo y se notifica el error.
 */
@RunWith(AndroidJUnit4::class)
class AlbumCreateE2ETest {

    private class FakeCreateService : BaseFakeVinilosApiService() {
        @Volatile var albums: List<Album> = initialAlbums
        @Volatile var failCreate = false

        override suspend fun getAlbums(): List<Album> = albums
        override suspend fun getAlbum(albumId: Int): Album =
            albums.firstOrNull { it.id == albumId }
                ?: throw NoSuchElementException("Album $albumId not found")

        override suspend fun createAlbum(album: Album): Album {
            if (failCreate) throw IOException("Network unavailable")
            val created = album.copy(id = albums.maxOf { it.id } + 1)
            albums = albums + created
            return created
        }

        fun reset() {
            albums = initialAlbums
            failCreate = false
        }
    }

    companion object {
        private val initialAlbums = listOf(
            Album(
                id = 100,
                name = "Buscando América",
                cover = "",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Primer álbum de Rubén Blades y Seis del Solar.",
                genre = "Salsa",
                recordLabel = "Elektra",
                performers = listOf(Performer(id = 100, name = "Rubén Blades Bellido de Luna")),
            ),
            Album(
                id = 101,
                name = "A Night at the Opera",
                cover = "",
                releaseDate = "1975-11-21T00:00:00.000Z",
                description = "El cuarto álbum de estudio de Queen.",
                genre = "Rock",
                recordLabel = "EMI",
                performers = listOf(Performer(id = 101, name = "Queen")),
            ),
        )

        private val fakeService = FakeCreateService()

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

    // Se reinicia el servicio antes de lanzar la actividad para que cada escenario parta del mismo catálogo.
    @get:Rule(order = 0)
    val resetService = object : ExternalResource() {
        override fun before() = fakeService.reset()
    }

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openCreateForm() {
        waitForText("Buscando América")
        composeTestRule.onNodeWithContentDescription("Agregar álbum").performClick()
        waitForText("Nuevo Álbum")
    }

    private fun fillValidAlbumForm() {
        composeTestRule.onNodeWithContentDescription("Nombre del álbum")
            .performScrollTo().performTextInput("Álbum 2024")
        composeTestRule.onNodeWithContentDescription("URL de la carátula")
            .performScrollTo().performTextInput("https://example.invalid/cover.jpg")
        composeTestRule.onNodeWithContentDescription("Fecha de lanzamiento")
            .performScrollTo().performClick()
        composeTestRule.onAllNodes(hasClickAction() and hasText("15", substring = true))
            .onFirst().performClick()
        composeTestRule.onNodeWithText("OK").performClick()
        // La fila de géneros se desplaza horizontalmente: se lleva a la vista el campo siguiente
        // para que los chips no queden bajo la barra fija de acciones.
        composeTestRule.onNodeWithContentDescription("Sello discográfico").performScrollTo()
        composeTestRule.onNodeWithText("Folk").performScrollTo().performClick()
        composeTestRule.onNodeWithContentDescription("Sello discográfico")
            .performScrollTo().performClick()
        composeTestRule.onNodeWithText("Sony Music").performClick()
        composeTestRule.onNodeWithContentDescription("Descripción curatorial")
            .performScrollTo().performTextInput("Música folclórica, edición 2024.")
    }

    @Test
    fun crearAlbum_flujoCompleto_confirmaCreacionMuestraEnCatalogoYAbreDetalle() {
        openCreateForm()
        fillValidAlbumForm()
        composeTestRule.onNodeWithText("Guardar Álbum").performClick()

        waitForText("Álbum creado exitosamente")
        waitForText("Explora 3 álbumes clásicos")
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Álbum 2024"))
        composeTestRule.onNodeWithText("Álbum 2024").assertIsDisplayed()
        val created = fakeService.albums.last()
        assertEquals("Álbum 2024", created.name)
        assertEquals("Folk", created.genre)
        assertEquals("Sony Music", created.recordLabel)

        composeTestRule.onNodeWithText("VER ÁLBUM").performClick()
        waitForText("Detalle de Álbum")
        composeTestRule.onNodeWithText("Álbum 2024").assertIsDisplayed()
    }

    @Test
    fun crearAlbum_formularioIncompleto_noCreaYSenalaCamposFaltantes() {
        openCreateForm()
        composeTestRule.onNodeWithContentDescription("Nombre del álbum").performTextInput("Álbum 2024")
        composeTestRule.onNodeWithText("Guardar Álbum").performClick()

        composeTestRule.onNodeWithText("Revisa los campos obligatorios y corrige los errores.")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Este campo es obligatorio.").assertCountEquals(5)
        composeTestRule.onNodeWithText("Nuevo Álbum").assertIsDisplayed()
        assertEquals(initialAlbums, fakeService.albums)
    }

    @Test
    fun crearAlbum_cancelarConBoton_noCreaYNotificaCancelacion() {
        openCreateForm()
        fillValidAlbumForm()
        composeTestRule.onNodeWithText("Cancelar").performClick()

        waitForText("Se canceló la creación del álbum.")
        composeTestRule.onNodeWithText("Explora 2 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertDoesNotExist()
        assertEquals(initialAlbums, fakeService.albums)
    }

    @Test
    fun crearAlbum_atrasDelSistema_noCreaYNotificaCancelacion() {
        openCreateForm()
        composeTestRule.onNodeWithContentDescription("Nombre del álbum").performTextInput("Álbum 2024")
        // Se usa el dispatcher para no depender de que el teclado consuma el primer "atrás".
        composeTestRule.runOnUiThread {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        waitForText("Se canceló la creación del álbum.")
        composeTestRule.onNodeWithText("Explora 2 álbumes clásicos").assertIsDisplayed()
        assertEquals(initialAlbums, fakeService.albums)
    }

    @Test
    fun crearAlbum_errorDelServidor_noGuardaRedirigeAlCatalogoYNotifica() {
        fakeService.failCreate = true
        openCreateForm()
        fillValidAlbumForm()
        composeTestRule.onNodeWithText("Guardar Álbum").performClick()

        waitForText("No se pudo crear el álbum. Inténtalo de nuevo.")
        waitForText("Explora 2 álbumes clásicos")
        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertDoesNotExist()
        assertEquals(initialAlbums, fakeService.albums)
    }
}
