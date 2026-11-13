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
import com.example.misw4203_tsdc_vinilos.data.network.BandDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.MusicianDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Pruebas End-to-End (E2E) instrumentadas con Jetpack Compose UI Test para el listado de artistas (HU03).
 */
@RunWith(AndroidJUnit4::class)
class ArtistListFlowE2ETest {

    private class ArtistFakeService : BaseFakeVinilosApiService() {
        var musicians: List<MusicianDetailResponse> = emptyList()
        var bands: List<BandDetailResponse> = emptyList()
        var error: Exception? = null

        override suspend fun getMusicians(): List<MusicianDetailResponse> {
            error?.let { throw it }
            return musicians
        }

        override suspend fun getMusician(musicianId: Int): MusicianDetailResponse {
            error?.let { throw it }
            return musicians.firstOrNull { it.id == musicianId }
                ?: throw NoSuchElementException("Musician $musicianId not found")
        }

        override suspend fun getBands(): List<BandDetailResponse> {
            error?.let { throw it }
            return bands
        }

        override suspend fun getBand(bandId: Int): BandDetailResponse {
            error?.let { throw it }
            return bands.firstOrNull { it.id == bandId }
                ?: throw NoSuchElementException("Band $bandId not found")
        }

        fun reset() {
            musicians = emptyList()
            bands = emptyList()
            error = null
        }
    }

    companion object {
        private val fakeService = ArtistFakeService()

        private val sampleMusicians = listOf(
            MusicianDetailResponse(
                id = 1,
                name = "Joan Manuel Serrat",
                birthDate = "1943-12-27T05:00:00.000Z",
                description = "Cantautor español.",
            )
        )

        private val sampleBands = listOf(
            BandDetailResponse(
                id = 2,
                name = "Soda Stereo",
                creationDate = "1982-01-01T05:00:00.000Z",
                description = "Rock en Español.",
            )
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

    private fun openArtistsTab(
        musicians: List<MusicianDetailResponse> = emptyList(),
        bands: List<BandDetailResponse> = emptyList(),
        error: Exception? = null,
    ) {
        fakeService.reset()
        fakeService.musicians = musicians
        fakeService.bands = bands
        fakeService.error = error
        composeTestRule.onNodeWithText("Artistas").performClick()
    }

    @Test
    fun flujoCompleto_listadoConFiltro_navegacionAlDetalle_yRetornoAlListado() {
        openArtistsTab(musicians = sampleMusicians, bands = sampleBands)
        waitForText("Soda Stereo")

        composeTestRule.onNodeWithText("Artistas").assertIsSelected()
        composeTestRule.onNodeWithText("Mostrando 2 artistas • CATÁLOGO VINILOS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Soda Stereo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Joan Manuel Serrat").assertIsDisplayed()

        // Filtrar por nombre
        composeTestRule.onNodeWithText("Buscar artista o banda…").performTextInput("Soda")
        waitForText("Mostrando 1 artista • CATÁLOGO VINILOS")
        composeTestRule.onNodeWithText("Soda Stereo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Joan Manuel Serrat").assertDoesNotExist()

        // Limpiar filtro
        composeTestRule.onNodeWithContentDescription("Limpiar búsqueda de artistas").performClick()
        waitForText("Mostrando 2 artistas • CATÁLOGO VINILOS")

        // Navegar al detalle del artista
        composeTestRule.onNodeWithContentDescription("Ver detalle de Soda Stereo").performClick()
        waitForText("Detalle de Artista")
        composeTestRule.onNodeWithText("Soda Stereo").assertIsDisplayed()

        // Regresar
        composeTestRule.onNodeWithContentDescription("Volver atrás").performClick()
        waitForText("Mostrando 2 artistas • CATÁLOGO VINILOS")
    }

    @Test
    fun listado_paginacionCliente_navegaEntrePaginas() {
        val musicians = (1..10).map { MusicianDetailResponse(id = it, name = "Solista $it", birthDate = "1980-01-01") }
        val bands = (11..15).map { BandDetailResponse(id = it, name = "Banda $it", creationDate = "1990-01-01") }

        openArtistsTab(musicians = musicians, bands = bands)
        waitForText("Mostrando 15 artistas • CATÁLOGO VINILOS")

        // Página 1:
        composeTestRule.onNodeWithText("Solista 1").assertIsDisplayed()
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Pág. 1 de 2"))
        composeTestRule.onNodeWithText("Banda 11").assertDoesNotExist()

        // Página 2:
        composeTestRule.onNodeWithContentDescription("Página siguiente").performClick()
        waitForText("Banda 11")
        composeTestRule.onNodeWithText("Solista 1").assertDoesNotExist()

        // Regresar a Página 1:
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Pág. 2 de 2"))
        composeTestRule.onNodeWithContentDescription("Página anterior").performClick()
        waitForText("Solista 1")
    }

    @Test
    fun listado_sinRegistros_muestraMensajeVacio() {
        openArtistsTab(musicians = emptyList(), bands = emptyList())

        waitForText("No hay registros disponibles.")
        composeTestRule.onNodeWithText("No hay registros disponibles.").assertIsDisplayed()
    }

    @Test
    fun listado_conError_muestraMensajeErrorYPermiteReintentar() {
        val errorMessage = "Ocurrió un error al cargar los artistas. Por favor, comunícate con el administrador."
        openArtistsTab(error = IOException("Red no disponible"))

        waitForText(errorMessage)
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        fakeService.error = null
        fakeService.musicians = sampleMusicians
        fakeService.bands = sampleBands
        composeTestRule.onNodeWithText("Reintentar").performClick()

        waitForText("Soda Stereo")
        composeTestRule.onNodeWithText("Mostrando 2 artistas • CATÁLOGO VINILOS").assertIsDisplayed()
    }
}
