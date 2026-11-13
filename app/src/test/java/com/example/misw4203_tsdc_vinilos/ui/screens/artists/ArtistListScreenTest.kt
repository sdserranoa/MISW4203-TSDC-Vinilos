package com.example.misw4203_tsdc_vinilos.ui.screens.artists

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class ArtistListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val soda = Performer(1, "Soda Stereo", description = "Rock en Español", creationDate = "1982-01-01")
    private val serrat = Performer(2, "Joan Manuel Serrat", description = "Cantautor", birthDate = "1943-12-27")

    private fun successState(
        artists: List<Performer> = listOf(soda, serrat),
        totalArtists: Int = artists.size,
        currentPage: Int = 1,
        totalPages: Int = 1,
        searchQuery: String = "",
        filterType: ArtistFilterType = ArtistFilterType.ALL,
    ) = ArtistListUiState.Success(artists, totalArtists, currentPage, totalPages, searchQuery, filterType)

    private fun setContent(
        uiState: ArtistListUiState,
        onPreviousPage: () -> Unit = {},
        onNextPage: () -> Unit = {},
        onRetry: () -> Unit = {},
        onSearchQueryChange: (String) -> Unit = {},
        onFilterTypeChange: (ArtistFilterType) -> Unit = {},
        onArtistClick: (Int, Boolean) -> Unit = { _, _ -> },
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                ArtistListContent(
                    uiState = uiState,
                    onPreviousPage = onPreviousPage,
                    onNextPage = onNextPage,
                    onRetry = onRetry,
                    onSearchQueryChange = onSearchQueryChange,
                    onFilterTypeChange = onFilterTypeChange,
                    onArtistClick = onArtistClick,
                )
            }
        }
    }

    @Test
    fun exito_muestraNombreYEtiquetaDeCadaArtista() {
        setContent(successState())

        composeTestRule.onNodeWithText("Mostrando 2 artistas • CATÁLOGO VINILOS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Soda Stereo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banda").assertIsDisplayed()
        composeTestRule.onNodeWithText("Joan Manuel Serrat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solista").assertIsDisplayed()
    }

    @Test
    fun tarjeta_alTocarla_navegaAlDetalle() {
        var clickedId: Int? = null
        var clickedIsBand: Boolean? = null
        setContent(successState(), onArtistClick = { id, isBand ->
            clickedId = id
            clickedIsBand = isBand
        })

        composeTestRule.onNodeWithText("Soda Stereo").performClick()

        assertEquals(1, clickedId)
        assertEquals(true, clickedIsBand)
    }

    @Test
    fun filtros_alCambiarPestanaDeFiltro_notificaFiltroSeleccionado() {
        var selectedFilter: ArtistFilterType? = null
        setContent(successState(), onFilterTypeChange = { selectedFilter = it })

        composeTestRule.onNodeWithText("Bandas").performClick()

        assertEquals(ArtistFilterType.BANDS, selectedFilter)
    }

    @Test
    fun busqueda_alEscribir_notificaTextoBuscado() {
        var query = ""
        setContent(successState(), onSearchQueryChange = { query = it })

        composeTestRule.onNodeWithText("Buscar artista o banda…").performTextInput("Soda")

        assertEquals("Soda", query)
    }

    @Test
    fun paginacion_muestraPaginaActualYNavega() {
        var nextClicks = 0
        setContent(
            uiState = successState(totalArtists = 15, currentPage = 1, totalPages = 2),
            onNextPage = { nextClicks++ },
        )

        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Pág. 1 de 2"))
        composeTestRule.onNodeWithText("Pág. 1 de 2").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Página siguiente").assertIsEnabled().performClick()

        assertEquals(1, nextClicks)
    }

    @Test
    fun cargando_muestraIndicadorDeCarga() {
        setContent(ArtistListUiState.Loading)

        composeTestRule.onNodeWithText("Cargando artistas…").assertIsDisplayed()
    }

    @Test
    fun vacio_muestraMensajeDeNoHayRegistrosDisponibles() {
        setContent(ArtistListUiState.Empty)

        composeTestRule.onNodeWithText("No hay registros disponibles.").assertIsDisplayed()
    }

    @Test
    fun error_muestraMensajeDeContactoConElAdministradorYPermiteReintentar() {
        var retries = 0
        setContent(ArtistListUiState.Error, onRetry = { retries++ })

        composeTestRule.onNodeWithText(
            "Ocurrió un error al cargar los artistas. Por favor, comunícate con el administrador."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()

        assertEquals(1, retries)
    }
}
