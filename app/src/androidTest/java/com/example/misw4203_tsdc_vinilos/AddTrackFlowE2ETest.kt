package com.example.misw4203_tsdc_vinilos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.misw4203_tsdc_vinilos.data.network.TrackRequest
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Track
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas End-to-End (E2E) instrumentadas para la HU08: Asociar canciones a un álbum.
 */
@RunWith(AndroidJUnit4::class)
class AddTrackFlowE2ETest {

    private class AddTrackFakeService : BaseFakeVinilosApiService() {
        var albumsList = mutableListOf(
            Album(
                id = 100,
                name = "Buscando América",
                cover = "",
                releaseDate = "1984-08-01T00:00:00.000Z",
                description = "Álbum clásico de Rubén Blades.",
                genre = "Salsa",
                recordLabel = "Elektra",
                tracks = emptyList(),
            )
        )

        override suspend fun getAlbums(): List<Album> = albumsList

        override suspend fun getAlbum(albumId: Int): Album =
            albumsList.firstOrNull { it.id == albumId }
                ?: throw NoSuchElementException("Album $albumId not found")

        override suspend fun postTrack(albumId: Int, track: TrackRequest): Track {
            val newTrack = Track(id = 200, name = track.name, duration = track.duration)
            albumsList = albumsList.map { album ->
                if (album.id == albumId) {
                    album.copy(tracks = album.tracks + newTrack)
                } else album
            }.toMutableList()
            return newTrack
        }
    }

    companion object {
        private val fakeService = AddTrackFakeService()

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

    @Test
    fun flujoAsociarCancion_exito_agregaCancionYRegresaAlDetalleDelAlbum() {
        // 1. Esperar al catálogo y seleccionar el álbum
        waitForText("Buscando América")
        composeTestRule.onNodeWithText("Buscando América").performClick()

        // 2. Verificar detalle del álbum
        waitForText("Detalle de Álbum")
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()

        // 3. Hacer clic en "+ Agregar canción"
        composeTestRule.onNodeWithText("+ Agregar canción").performClick()

        // 4. Formulario de asociación
        waitForText("Agregar Canción")
        composeTestRule.onNodeWithText("Agregar Canción").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()

        // 5. Llenar los campos de la canción (Criterio 2)
        composeTestRule.onNodeWithText("Come Together").performTextInput("Decisiones")
        composeTestRule.onNodeWithText("MM:SS (ej. 04:20)").performTextInput("5:05")

        // 6. Confirmar la asociación (Criterio 4)
        composeTestRule.onNodeWithText("+ Asociar Canción").performClick()

        // 7. Redirección al detalle del álbum con la canción asociada
        waitForText("Detalle de Álbum")
        waitForText("Decisiones")
        composeTestRule.onNodeWithText("Decisiones").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("5:05").assertIsDisplayed()
    }

    @Test
    fun flujoAsociarCancion_cancelar_regresaAlDetalleSinModificar() {
        // 1. Ir al detalle del álbum
        waitForText("Buscando América")
        composeTestRule.onNodeWithText("Buscando América").performClick()
        waitForText("Detalle de Álbum")

        // 2. Ir a asociar canción y cancelar (Criterio 5)
        composeTestRule.onNodeWithText("+ Agregar canción").performClick()
        waitForText("Agregar Canción")
        composeTestRule.onNodeWithText("Cancelar").performClick()

        // 3. Retorno al detalle del álbum sin modificaciones
        waitForText("Detalle de Álbum")
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
    }
}
