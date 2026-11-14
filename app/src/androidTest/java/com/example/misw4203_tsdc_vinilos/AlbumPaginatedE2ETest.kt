package com.example.misw4203_tsdc_vinilos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Performer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas End-to-End (E2E) instrumentadas para validar la paginación del catálogo de álbumes (HU01).
 *
 * Como el backend expone datos sin paginar (`GET /albums` retorna la lista completa), la aplicación
 * realiza la paginación en el cliente (10 álbumes por página).
 *
 * Esta prueba valida:
 * 1. La carga inicial con más de 10 álbumes (15 álbumes de prueba -> 2 páginas).
 * 2. Visualización de los primeros 10 álbumes en la Página 1.
 * 3. Desplazamiento (scroll) por el listado hasta la barra de paginación.
 * 4. Navegación a la Página 2 y verificación de que los álbumes corresponden a los datos de la segunda página.
 * 5. Confirmación de que los álbumes de la Página 1 ya no están visibles en la Página 2.
 * 6. Navegación de regreso a la Página 1 y restauración de los elementos correspondientes.
 */
@RunWith(AndroidJUnit4::class)
class AlbumPaginatedE2ETest {

    companion object {
        private fun generateTestAlbums(count: Int): List<Album> = (1..count).map { id ->
            Album(
                id = id,
                name = "Álbum $id",
                cover = "",
                releaseDate = "1980-01-01T00:00:00.000Z",
                description = "Descripción del álbum $id",
                genre = if (id % 2 == 0) "Rock" else "Salsa",
                recordLabel = "Fania",
                performers = listOf(Performer(id = id, name = "Artista $id")),
            )
        }

        private val testAlbums = generateTestAlbums(15)

        private val fakeService = object : BaseFakeVinilosApiService() {
            override suspend fun getAlbums(): List<Album> = testAlbums
            override suspend fun getAlbum(albumId: Int): Album =
                testAlbums.firstOrNull { it.id == albumId }
                    ?: throw NoSuchElementException("Album $albumId not found")
            override suspend fun createAlbum(album: Album): Album = album
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
    fun catalogo_scrollYNavegacionPaginada_muestraCorrectamenteAlbumesDePagina2() {
        // 1. Esperar a que el catálogo cargue los datos iniciales
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("Álbum 1").fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Verificar datos de la Página 1 (deben mostrarse álbumes 1 al 10, y no los de la página 2)
        composeTestRule.onNodeWithText("Vinilos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum 11").assertDoesNotExist()
        composeTestRule.onNodeWithText("Álbum 15").assertDoesNotExist()

        // 3. Hacer scroll en la cuadrícula hasta la barra de paginación
        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("Pág. 1 de 2"))
        composeTestRule.onNodeWithText("Pág. 1 de 2").assertIsDisplayed()

        // 4. Navegar a la segunda página utilizando el botón de página siguiente
        composeTestRule.onNodeWithContentDescription("Página siguiente").performClick()

        // 5. Verificar que la Página 2 se abre en el tope con los primeros álbumes (11 en adelante)
        composeTestRule.onNodeWithText("Álbum 11").assertIsDisplayed()

        // 6. Verificar que los álbumes de la Página 1 ya no se muestran
        composeTestRule.onNodeWithText("Álbum 1").assertDoesNotExist()
        composeTestRule.onNodeWithText("Álbum 10").assertDoesNotExist()

        // 7. Hacer scroll en la Página 2 hasta la barra de paginación
        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("Pág. 2 de 2"))
        composeTestRule.onNodeWithText("Pág. 2 de 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum 15").assertIsDisplayed()

        // 8. Navegar de regreso a la Página 1 y validar el retorno
        composeTestRule.onNodeWithContentDescription("Página anterior").performClick()
        composeTestRule.onNodeWithText("Álbum 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum 11").assertDoesNotExist()
    }
}
