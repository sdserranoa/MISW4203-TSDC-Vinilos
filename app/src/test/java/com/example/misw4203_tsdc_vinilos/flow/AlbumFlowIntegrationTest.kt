package com.example.misw4203_tsdc_vinilos.flow

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.CommentCollector
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.Track
import com.example.misw4203_tsdc_vinilos.navigation.AlbumCreateRoute
import com.example.misw4203_tsdc_vinilos.navigation.AlbumListRoute
import com.example.misw4203_tsdc_vinilos.navigation.VinilosNavHost
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate.AlbumCreateViewModel
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

/**
 * Pruebas de integración del catálogo (HU01), detalle (HU02) y creación de álbumes (HU07),
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
 * 8. Creación de álbumes (HU07) desde el botón "Agregar álbum": creación exitosa con confirmación
 *    y catálogo actualizado, formulario incompleto, cancelación (botón y atrás del sistema)
 *    y retorno al catálogo con notificación cuando falla la creación.
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

    /** Diligencia todos los campos de HU07 a través de la interfaz, incluidos el calendario y los selectores. */
    private fun fillValidAlbumForm(name: String = "Álbum 2024") {
        composeTestRule.onNodeWithContentDescription("Nombre del álbum").performTextInput(name)
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
            .performScrollTo().performTextInput("Música clásica, edición 2024.")
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

    @Test
    fun errorAlCrearAlbum_regresaAlCatalogoYNotificaSinGuardar() {
        lateinit var navController: NavHostController
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                navController = rememberNavController()
                VinilosNavHost(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Buscando América").performClick()
        composeTestRule.runOnIdle {
            navController.navigate(AlbumCreateRoute)
        }
        composeTestRule.onNodeWithText("Nuevo Álbum").assertIsDisplayed()
        composeTestRule.runOnIdle {
            val entry = navController.getBackStackEntry(AlbumCreateRoute)
            val viewModel = ViewModelProvider(entry, AlbumCreateViewModel.Factory)
                .get(AlbumCreateViewModel::class.java)
            viewModel.onNameChange("Álbum 2024")
            viewModel.onCoverChange("https://example.invalid/cover.jpg")
            viewModel.onReleaseDateChange("2024-02-29")
            viewModel.onGenreChange("Rock")
            viewModel.onRecordLabelChange("Sony Music")
            viewModel.onDescriptionChange("Música clásica, edición 2024.")
            fakeService.createError = IOException("Network unavailable")
        }

        composeTestRule.onNodeWithText("Guardar Álbum").performClick()

        composeTestRule.onNodeWithText("No se pudo crear el álbum. Inténtalo de nuevo.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertTrue(navController.currentDestination?.hasRoute<AlbumListRoute>() == true)
            assertNull(navController.previousBackStackEntry)
            assertEquals(sampleAlbums, fakeService.albums)
        }
    }

    @Test
    fun crearAlbum_desdeCatalogo_confirmaCreacionYMuestraAlbumEnElListado() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Agregar álbum").performClick()
        composeTestRule.onNodeWithText("Nuevo Álbum").assertIsDisplayed()
        fillValidAlbumForm()
        composeTestRule.onNodeWithText("Guardar Álbum").performClick()

        // Confirmación de creación exitosa y catálogo actualizado con el nuevo álbum
        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 4 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Álbum 2024"))
        composeTestRule.onNodeWithText("Álbum 2024").assertIsDisplayed()
        val created = fakeService.albums.last()
        assertEquals("Álbum 2024", created.name)
        assertEquals("Folk", created.genre)
        assertEquals("Sony Music", created.recordLabel)

        // La acción de la notificación abre el detalle del álbum creado
        composeTestRule.onNodeWithText("VER ÁLBUM").performClick()
        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum 2024").assertIsDisplayed()
    }

    @Test
    fun crearAlbum_formularioIncompleto_noCreaYPermaneceEnElFormulario() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Agregar álbum").performClick()
        composeTestRule.onNodeWithContentDescription("Nombre del álbum").performTextInput("Álbum 2024")
        composeTestRule.onNodeWithText("Guardar Álbum").performClick()

        composeTestRule.onNodeWithText("Revisa los campos obligatorios y corrige los errores.")
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Este campo es obligatorio.").assertCountEquals(5)
        composeTestRule.onNodeWithText("Nuevo Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertDoesNotExist()
        assertEquals(sampleAlbums, fakeService.albums)
    }

    @Test
    fun crearAlbum_cancelarConBoton_noCreaYNotificaCancelacion() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Agregar álbum").performClick()
        fillValidAlbumForm()
        composeTestRule.onNodeWithText("Cancelar").performClick()

        composeTestRule.onNodeWithText("Se canceló la creación del álbum.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertDoesNotExist()
        assertEquals(sampleAlbums, fakeService.albums)
    }

    @Test
    fun crearAlbum_atrasDelSistema_noCreaYNotificaCancelacion() {
        lateinit var backDispatcher: OnBackPressedDispatcher
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                backDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher
                VinilosNavHost()
            }
        }

        composeTestRule.onNodeWithContentDescription("Agregar álbum").performClick()
        composeTestRule.onNodeWithContentDescription("Nombre del álbum").performTextInput("Álbum 2024")
        composeTestRule.runOnIdle { backDispatcher.onBackPressed() }

        composeTestRule.onNodeWithText("Se canceló la creación del álbum.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()
        assertEquals(sampleAlbums, fakeService.albums)
    }

    @Test
    fun crearAlbum_errorDelServidorDesdeCatalogo_regresaAlCatalogoYNotificaSinGuardar() {
        setContent()
        fakeService.createError = IOException("Network unavailable")

        composeTestRule.onNodeWithContentDescription("Agregar álbum").performClick()
        fillValidAlbumForm()
        composeTestRule.onNodeWithText("Guardar Álbum").performClick()

        composeTestRule.onNodeWithText("No se pudo crear el álbum. Inténtalo de nuevo.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Explora 3 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum creado exitosamente").assertDoesNotExist()
        assertEquals(sampleAlbums, fakeService.albums)
    }
}
