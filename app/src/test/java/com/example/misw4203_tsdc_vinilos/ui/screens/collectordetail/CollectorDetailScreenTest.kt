package com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.misw4203_tsdc_vinilos.model.CollectorDetail
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de interfaz en Jetpack Compose del detalle de un coleccionista (HU06), ejecutadas en la
 * JVM con Robolectric sobre una pantalla de teléfono de 411 x 891 dp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class CollectorDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val manolo = CollectorDetail(
        id = 100,
        name = "Manolo Bellon",
        telephone = "3502457896",
        email = "manollo@caracol.com.co",
        albums = listOf(
            testAlbum(100, name = "Buscando América", genre = "Salsa", releaseDate = "1984-08-01T00:00:00.000Z"),
            testAlbum(102, name = "A Night at the Opera", genre = "Rock", releaseDate = "1975-11-21T00:00:00.000Z"),
        ),
        favoritePerformers = listOf(
            Performer(100, "Rubén Blades Bellido de Luna", birthDate = "1948-07-16T00:00:00.000Z"),
            Performer(101, "Queen", creationDate = "1970-01-01T00:00:00.000Z"),
        ),
        comments = listOf(Comment(100, "The most relevant album of Ruben Blades", 4)),
    )

    private val withoutRecords = manolo.copy(albums = emptyList(), favoritePerformers = emptyList(), comments = emptyList())

    private fun setContent(
        uiState: CollectorDetailUiState,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
        onAlbumClick: (Int) -> Unit = {},
        onPerformerClick: ((Int, Boolean) -> Unit)? = { _, _ -> },
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                CollectorDetailContent(
                    uiState = uiState,
                    onBack = onBack,
                    onRetry = onRetry,
                    onAlbumClick = onAlbumClick,
                    onPerformerClick = onPerformerClick,
                )
            }
        }
    }

    private fun scrollTo(text: String) {
        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))
    }

    @Test
    fun exito_muestraNombreTelefonoYCorreoDelColeccionista() {
        setContent(CollectorDetailUiState.Success(manolo))

        composeTestRule.onNodeWithText("Detalle de Coleccionista").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithText("MB").assertIsDisplayed()
        composeTestRule.onNodeWithText("3502457896").assertIsDisplayed()
        composeTestRule.onNodeWithText("manollo@caracol.com.co").assertIsDisplayed()
    }

    @Test
    fun exito_muestraLosTotalesYLasPestanasConSuCantidad() {
        setContent(CollectorDetailUiState.Success(manolo))

        composeTestRule.onNodeWithText("Favoritos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbumes (2)").assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithText("Artistas (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Comentarios (1)").assertIsDisplayed()
    }

    @Test
    fun pestanaAlbumes_muestraNombreCaratulaGeneroYAnio() {
        setContent(CollectorDetailUiState.Success(manolo))

        scrollTo("Buscando América")
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Carátula del álbum Buscando América").assertExists()
        composeTestRule.onNodeWithText("Salsa").assertIsDisplayed()
        composeTestRule.onNodeWithText("1984").assertIsDisplayed()
        composeTestRule.onNodeWithText("A Night at the Opera").assertIsDisplayed()
        composeTestRule.onNodeWithText("1975").assertIsDisplayed()
    }

    @Test
    fun album_alTocarlo_navegaAlDetalleDelAlbum() {
        var albumId: Int? = null
        setContent(CollectorDetailUiState.Success(manolo), onAlbumClick = { albumId = it })

        scrollTo("A Night at the Opera")
        composeTestRule.onNodeWithText("A Night at the Opera").performClick()

        assertEquals(102, albumId)
    }

    @Test
    fun pestanaArtistas_muestraNombreYTipoDeCadaFavorito() {
        setContent(CollectorDetailUiState.Success(manolo))

        composeTestRule.onNodeWithText("Artistas (2)").performClick()

        composeTestRule.onNodeWithText("Artistas (2)").assertIsSelected()
        scrollTo("Queen")
        composeTestRule.onNodeWithText("Rubén Blades Bellido de Luna").assertIsDisplayed()
        composeTestRule.onNodeWithText("Músico / Solista").assertIsDisplayed()
        composeTestRule.onNodeWithText("Queen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banda / Agrupación").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertDoesNotExist()
    }

    @Test
    fun artista_alTocarlo_navegaConSuIdYTipo() {
        var clicked: Pair<Int, Boolean>? = null
        setContent(CollectorDetailUiState.Success(manolo), onPerformerClick = { id, isBand -> clicked = id to isBand })
        composeTestRule.onNodeWithText("Artistas (2)").performClick()

        scrollTo("Queen")
        composeTestRule.onNodeWithText("Queen").performClick()

        assertEquals(101 to true, clicked)
    }

    @Test
    fun artista_sinNavegacionDisponible_noEsInteractivo() {
        setContent(CollectorDetailUiState.Success(manolo), onPerformerClick = null)
        composeTestRule.onNodeWithText("Artistas (2)").performClick()

        scrollTo("Queen")
        composeTestRule.onNodeWithText("Queen").assertHasNoClickAction()
        composeTestRule.onNodeWithContentDescription("Ver detalle de Queen").assertDoesNotExist()
    }

    @Test
    fun pestanaComentarios_muestraElComentarioYSuCalificacion() {
        setContent(CollectorDetailUiState.Success(manolo))

        composeTestRule.onNodeWithText("Comentarios (1)").performClick()

        scrollTo("The most relevant album of Ruben Blades")
        composeTestRule.onNodeWithText("The most relevant album of Ruben Blades").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Calificación: 4 de 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("★★★★☆").assertIsDisplayed()
    }

    @Test
    fun sinAlbumes_muestraMensajeDeNoHayRegistros() {
        setContent(CollectorDetailUiState.Success(withoutRecords))

        scrollTo("No hay álbumes registrados para este coleccionista.")
        composeTestRule.onNodeWithText("No hay álbumes registrados para este coleccionista.").assertIsDisplayed()
    }

    @Test
    fun sinArtistasFavoritos_muestraMensajeDeNoHayRegistros() {
        setContent(CollectorDetailUiState.Success(withoutRecords))

        composeTestRule.onNodeWithText("Artistas (0)").performClick()

        scrollTo("No hay artistas favoritos registrados para este coleccionista.")
        composeTestRule.onNodeWithText("No hay artistas favoritos registrados para este coleccionista.")
            .assertIsDisplayed()
    }

    @Test
    fun sinComentarios_muestraMensajeDeNoHayRegistros() {
        setContent(CollectorDetailUiState.Success(withoutRecords))

        composeTestRule.onNodeWithText("Comentarios (0)").performClick()

        scrollTo("No hay comentarios registrados para este coleccionista.")
        composeTestRule.onNodeWithText("No hay comentarios registrados para este coleccionista.")
            .assertIsDisplayed()
    }

    @Test
    fun botonVolver_invocaLaNavegacionHaciaAtras() {
        var backClicks = 0
        setContent(CollectorDetailUiState.Success(manolo), onBack = { backClicks++ })

        composeTestRule.onNodeWithContentDescription("Volver a coleccionistas").performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun cargando_muestraIndicadorDeCarga() {
        setContent(CollectorDetailUiState.Loading)

        composeTestRule.onNodeWithText("Cargando información del coleccionista…").assertIsDisplayed()
    }

    @Test
    fun error_muestraMensajeDeContactoConElAdministradorYPermiteReintentar() {
        var retries = 0
        setContent(CollectorDetailUiState.Error, onRetry = { retries++ })

        composeTestRule.onNodeWithText(
            "Ocurrió un error al cargar el coleccionista. Por favor, comunícate con el administrador."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()

        assertEquals(1, retries)
    }
}
