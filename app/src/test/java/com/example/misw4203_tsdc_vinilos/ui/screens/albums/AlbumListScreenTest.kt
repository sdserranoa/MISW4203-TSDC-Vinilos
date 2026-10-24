package com.example.misw4203_tsdc_vinilos.ui.screens.albums

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
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.testutil.testAlbums
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de interfaz en Jetpack Compose del catálogo de álbumes (HU01), ejecutadas en la JVM
 * con Robolectric sobre una pantalla de teléfono de 411 x 891 dp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class AlbumListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: AlbumListUiState,
        onPreviousPage: () -> Unit = {},
        onNextPage: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                AlbumListContent(
                    uiState = uiState,
                    onPreviousPage = onPreviousPage,
                    onNextPage = onNextPage,
                    onRetry = onRetry,
                )
            }
        }
    }

    @Test
    fun exito_muestraNombreCaratulaGeneroYAnioDeCadaAlbum() {
        setContent(
            AlbumListUiState.Success(
                albums = listOf(
                    testAlbum(1, "Buscando América", "Salsa", "1984-08-01T00:00:00.000Z",
                        listOf(Performer(1, "Rubén Blades"))),
                    testAlbum(2, "Artaud", "Rock", "1973-01-01T00:00:00.000Z"),
                ),
                totalAlbums = 2,
                currentPage = 1,
                totalPages = 1,
            )
        )

        composeTestRule.onNodeWithText("Explora 2 álbumes clásicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rubén Blades").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salsa").assertIsDisplayed()
        composeTestRule.onNodeWithText("1984").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Carátula del álbum Buscando América")
            .assertExists()
        composeTestRule.onNodeWithText("Artaud").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rock").assertIsDisplayed()
        composeTestRule.onNodeWithText("1973").assertIsDisplayed()
    }

    @Test
    fun paginacion_muestraPaginaActualYNavegaEnAmbasDirecciones() {
        var previousClicks = 0
        var nextClicks = 0
        setContent(
            uiState = AlbumListUiState.Success(
                albums = testAlbums(2),
                totalAlbums = 25,
                currentPage = 2,
                totalPages = 3,
            ),
            onPreviousPage = { previousClicks++ },
            onNextPage = { nextClicks++ },
        )

        composeTestRule.onNodeWithText("Pág. 2 de 3").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Página anterior").performClick()
        composeTestRule.onNodeWithContentDescription("Página siguiente").performClick()

        assertEquals(1, previousClicks)
        assertEquals(1, nextClicks)
    }

    @Test
    fun paginacion_enPrimeraPagina_deshabilitaBotonAnterior() {
        setContent(
            AlbumListUiState.Success(
                albums = testAlbums(2),
                totalAlbums = 25,
                currentPage = 1,
                totalPages = 3,
            )
        )

        composeTestRule.onNodeWithContentDescription("Página anterior").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Página siguiente").assertIsEnabled()
    }

    @Test
    fun paginacion_enUltimaPagina_deshabilitaBotonSiguiente() {
        setContent(
            AlbumListUiState.Success(
                albums = testAlbums(2),
                totalAlbums = 22,
                currentPage = 3,
                totalPages = 3,
            )
        )

        composeTestRule.onNodeWithContentDescription("Página anterior").assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Página siguiente").assertIsNotEnabled()
    }

    @Test
    fun cargando_muestraMensajeDeCarga() {
        setContent(AlbumListUiState.Loading)

        composeTestRule.onNodeWithText("Cargando álbumes…").assertIsDisplayed()
    }

    @Test
    fun sinRegistros_muestraMensajeDeNoDisponibles() {
        setContent(AlbumListUiState.Empty)

        composeTestRule.onNodeWithText("No hay álbumes disponibles en este momento.")
            .assertIsDisplayed()
    }

    @Test
    fun error_muestraMensajeParaContactarAlAdministradorYPermiteReintentar() {
        var retries = 0
        setContent(AlbumListUiState.Error, onRetry = { retries++ })

        composeTestRule.onNodeWithText(
            "Ocurrió un error al cargar los álbumes. Por favor, comunícate con el administrador."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun pantallaConViewModel_alAvanzarDePagina_muestraLosSiguientesAlbumes() {
        val viewModel = AlbumListViewModel(
            AlbumRepository(FakeVinilosApiService(albums = testAlbums(12)))
        )
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                AlbumListScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText("Pág. 1 de 2"))
        composeTestRule.onNodeWithContentDescription("Página siguiente").performClick()

        composeTestRule.onNodeWithText("Pág. 2 de 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum 11").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum 12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Álbum 1").assertDoesNotExist()
    }
}
