package com.example.misw4203_tsdc_vinilos.ui.screens.artistdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.misw4203_tsdc_vinilos.model.ArtistPrizeItem
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.testutil.testArtistDetail
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de interfaz en Jetpack Compose del detalle de artista (HU04),
 * ejecutadas en la JVM con Robolectric sobre una pantalla de teléfono 411 x 891 dp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class ArtistDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: ArtistDetailUiState,
        onBack: () -> Unit = {},
        onToggleFavorite: () -> Unit = {},
        onRetry: () -> Unit = {},
        onAlbumClick: (Int) -> Unit = {},
        onMusicianClick: (Int) -> Unit = {},
        onBandClick: (Int) -> Unit = {},
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                ArtistDetailContent(
                    uiState = uiState,
                    onBack = onBack,
                    onToggleFavorite = onToggleFavorite,
                    onRetry = onRetry,
                    onAlbumClick = onAlbumClick,
                    onMusicianClick = onMusicianClick,
                    onBandClick = onBandClick,
                )
            }
        }
    }

    @Test
    fun exito_muestraInformacionDelMusicoSolista() {
        val artist = testArtistDetail(
            id = 1,
            name = "Gustavo Cerati",
            description = "Cantautor, guitarrista y compositor argentino.",
            birthDate = "1959-08-11",
            isBand = false,
        )

        setContent(ArtistDetailUiState.Success(artist = artist, isFavorite = false))

        composeTestRule.onNodeWithText("Gustavo Cerati").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nacimiento: 1959-08-11").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solista").assertIsDisplayed()
        composeTestRule.onNodeWithText("Agregar a favoritos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cantautor, guitarrista y compositor argentino.").assertIsDisplayed()
    }

    @Test
    fun exito_muestraBandasRelacionadasSiEsSolista() {
        var clickedBandId = -1
        val relatedBand = Performer(id = 50, name = "Soda Stereo", creationDate = "1982-01-01")
        val artist = testArtistDetail(
            id = 1,
            name = "Gustavo Cerati",
            isBand = false,
            relatedBands = listOf(relatedBand),
        )

        setContent(
            uiState = ArtistDetailUiState.Success(artist = artist),
            onBandClick = { clickedBandId = it }
        )

        composeTestRule.onNodeWithText("Banda: Soda Stereo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banda: Soda Stereo").performClick()
        assertEquals(50, clickedBandId)
    }

    @Test
    fun exito_muestraIntegrantesSiEsUnaBanda() {
        var clickedMusicianId = -1
        val member1 = Performer(id = 10, name = "Gustavo Cerati")
        val member2 = Performer(id = 11, name = "Zeta Bosio")
        val artist = testArtistDetail(
            id = 50,
            name = "Soda Stereo",
            creationDate = "1982-01-01",
            isBand = true,
            members = listOf(member1, member2),
        )

        setContent(
            uiState = ArtistDetailUiState.Success(artist = artist),
            onMusicianClick = { clickedMusicianId = it }
        )

        composeTestRule.onNodeWithText("Soda Stereo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Creación: 1982-01-01").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banda").assertIsDisplayed()
        composeTestRule.onNodeWithText("Integrantes de la agrupación (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gustavo Cerati").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zeta Bosio").assertIsDisplayed()

        composeTestRule.onNodeWithText("Zeta Bosio").performClick()
        assertEquals(11, clickedMusicianId)
    }

    @Test
    fun exito_muestraAlbumesAsociados() {
        var clickedAlbumId = -1
        val album1 = testAlbum(id = 101, name = "Bocanada", releaseDate = "1999-06-28", genre = "Rock")
        val artist = testArtistDetail(
            id = 1,
            name = "Gustavo Cerati",
            albums = listOf(album1),
        )

        setContent(
            uiState = ArtistDetailUiState.Success(artist = artist),
            onAlbumClick = { clickedAlbumId = it }
        )

        composeTestRule.onNodeWithText("Álbumes asociados (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bocanada").assertIsDisplayed()

        composeTestRule.onNodeWithText("Bocanada").performClick()
        assertEquals(101, clickedAlbumId)
    }

    @Test
    fun exito_muestraPremiosYReconocimientos() {
        val prize = ArtistPrizeItem(
            id = 1,
            name = "Grammy Latino",
            organization = "LARAS",
            premiationDate = "2007-11-08",
            description = "Mejor Álbum de Rock Vocal"
        )
        val artist = testArtistDetail(
            id = 1,
            name = "Gustavo Cerati",
            prizes = listOf(prize),
        )

        setContent(ArtistDetailUiState.Success(artist = artist))

        composeTestRule.onNodeWithText("Premios & Reconocimientos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Grammy Latino").assertIsDisplayed()
        composeTestRule.onNodeWithText("2007").assertIsDisplayed()
        composeTestRule.onNodeWithText("LARAS").assertIsDisplayed()
    }

    @Test
    fun exito_muestraColeccionistasQueLoSiguen() {
        val collector = Collector(id = 200, name = "Mateo V.", email = "mateo@example.com")
        val artist = testArtistDetail(
            id = 1,
            name = "Gustavo Cerati",
            collectors = listOf(collector),
        )

        setContent(ArtistDetailUiState.Success(artist = artist))

        composeTestRule.onNodeWithText("Coleccionistas que lo siguen (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mateo V.").assertIsDisplayed()
    }

    @Test
    fun interaccion_alternaBotonFavoritos() {
        var favoriteToggled = false
        val artist = testArtistDetail(id = 1, name = "Gustavo Cerati")

        setContent(
            uiState = ArtistDetailUiState.Success(artist = artist, isFavorite = false),
            onToggleFavorite = { favoriteToggled = true }
        )

        composeTestRule.onNodeWithText("Agregar a favoritos").performClick()
        assertTrue(favoriteToggled)
    }

    @Test
    fun estadoCarga_muestraIndicador() {
        setContent(ArtistDetailUiState.Loading)

        composeTestRule.onNodeWithText("Cargando perfil del artista…").assertIsDisplayed()
    }

    @Test
    fun estadoError_muestraMensajeYReintentar() {
        var retryTriggered = false
        setContent(
            uiState = ArtistDetailUiState.Error,
            onRetry = { retryTriggered = true }
        )

        composeTestRule.onNodeWithText(
            "Ocurrió un error al cargar el artista. Por favor, comunícate con el administrador."
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText("Reintentar").performClick()
        assertTrue(retryTriggered)
    }
}
