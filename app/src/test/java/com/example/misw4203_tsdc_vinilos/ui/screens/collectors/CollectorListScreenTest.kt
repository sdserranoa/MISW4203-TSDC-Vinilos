package com.example.misw4203_tsdc_vinilos.ui.screens.collectors

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
import com.example.misw4203_tsdc_vinilos.model.CollectorSummary
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de interfaz en Jetpack Compose del listado de coleccionistas (HU05), ejecutadas en la
 * JVM con Robolectric sobre una pantalla de teléfono de 411 x 891 dp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class CollectorListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val manolo = CollectorSummary(100, "Manolo Bellon", "manollo@caracol.com.co", 1)
    private val jaime = CollectorSummary(101, "Jaime Monsalve", "jmonsalve@rtvc.com.co", 3)

    private fun successState(
        collectors: List<CollectorSummary> = listOf(manolo, jaime),
        totalCollectors: Int = collectors.size,
        currentPage: Int = 1,
        totalPages: Int = 1,
        searchQuery: String = "",
    ) = CollectorListUiState.Success(collectors, totalCollectors, currentPage, totalPages, searchQuery)

    private fun setContent(
        uiState: CollectorListUiState,
        onPreviousPage: () -> Unit = {},
        onNextPage: () -> Unit = {},
        onRetry: () -> Unit = {},
        onSearchQueryChange: (String) -> Unit = {},
        onCollectorClick: (Int) -> Unit = {},
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                CollectorListContent(
                    uiState = uiState,
                    onPreviousPage = onPreviousPage,
                    onNextPage = onNextPage,
                    onRetry = onRetry,
                    onSearchQueryChange = onSearchQueryChange,
                    onCollectorClick = onCollectorClick,
                )
            }
        }
    }

    @Test
    fun exito_muestraNombreCorreoYCantidadDeAlbumesDeCadaColeccionista() {
        setContent(successState())

        composeTestRule.onNodeWithText("2 coleccionistas registrados").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manolo Bellon").assertIsDisplayed()
        composeTestRule.onNodeWithText("manollo@caracol.com.co").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jaime Monsalve").assertIsDisplayed()
        composeTestRule.onNodeWithText("jmonsalve@rtvc.com.co").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 álbumes").assertIsDisplayed()
    }

    @Test
    fun exito_conUnSoloColeccionista_usaElEncabezadoEnSingular() {
        setContent(successState(collectors = listOf(manolo)))

        composeTestRule.onNodeWithText("1 coleccionista registrado").assertIsDisplayed()
    }

    @Test
    fun exito_muestraLasInicialesComoAvatar() {
        setContent(successState())

        composeTestRule.onNodeWithText("MB").assertIsDisplayed()
        composeTestRule.onNodeWithText("JM").assertIsDisplayed()
    }

    @Test
    fun botonDeDetalle_navegaConElIdDelColeccionista() {
        var clickedId: Int? = null
        setContent(successState(), onCollectorClick = { clickedId = it })

        composeTestRule.onNodeWithContentDescription("Ver detalle de Jaime Monsalve").performClick()

        assertEquals(101, clickedId)
    }

    @Test
    fun tarjeta_alTocarla_navegaAlDetalleDelColeccionista() {
        var clickedId: Int? = null
        setContent(successState(), onCollectorClick = { clickedId = it })

        composeTestRule.onNodeWithText("Manolo Bellon").performClick()

        assertEquals(100, clickedId)
    }

    @Test
    fun busqueda_alEscribir_notificaElTexto() {
        var query = ""
        setContent(successState(), onSearchQueryChange = { query = it })

        composeTestRule.onNodeWithText("Buscar coleccionista por nombre…").performTextInput("Jaime")

        assertEquals("Jaime", query)
    }

    @Test
    fun busqueda_conTexto_muestraBotonParaLimpiar() {
        var query = "Jaime"
        setContent(
            successState(collectors = listOf(jaime), searchQuery = "Jaime"),
            onSearchQueryChange = { query = it },
        )

        composeTestRule.onNodeWithContentDescription("Limpiar búsqueda de coleccionistas").performClick()

        assertEquals("", query)
    }

    @Test
    fun busqueda_sinCoincidencias_muestraMensajeConElTextoBuscado() {
        setContent(successState(collectors = emptyList(), searchQuery = "Inexistente"))

        composeTestRule.onNodeWithText("0 coleccionistas registrados").assertIsDisplayed()
        composeTestRule.onNodeWithText("No se encontraron coleccionistas para \"Inexistente\".")
            .assertIsDisplayed()
    }

    @Test
    fun paginacion_muestraPaginaActualYNavegaEnAmbasDirecciones() {
        var previousClicks = 0
        var nextClicks = 0
        setContent(
            uiState = successState(totalCollectors = 25, currentPage = 2, totalPages = 3),
            onPreviousPage = { previousClicks++ },
            onNextPage = { nextClicks++ },
        )

        composeTestRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Pág. 2 de 3"))
        composeTestRule.onNodeWithText("Pág. 2 de 3").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Página anterior").assertIsEnabled().performClick()
        composeTestRule.onNodeWithContentDescription("Página siguiente").assertIsEnabled().performClick()

        assertEquals(1, previousClicks)
        assertEquals(1, nextClicks)
    }

    @Test
    fun paginacion_enUnicaPagina_deshabilitaAmbosBotones() {
        setContent(successState())

        composeTestRule.onNodeWithContentDescription("Página anterior").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Página siguiente").assertIsNotEnabled()
    }

    @Test
    fun cargando_muestraIndicadorDeCarga() {
        setContent(CollectorListUiState.Loading)

        composeTestRule.onNodeWithText("Cargando coleccionistas…").assertIsDisplayed()
    }

    @Test
    fun vacio_muestraMensajeDeNoHayColeccionistasRegistrados() {
        setContent(CollectorListUiState.Empty)

        composeTestRule.onNodeWithText("No hay coleccionistas registrados.").assertIsDisplayed()
    }

    @Test
    fun error_muestraMensajeDeContactoConElAdministradorYPermiteReintentar() {
        var retries = 0
        setContent(CollectorListUiState.Error, onRetry = { retries++ })

        composeTestRule.onNodeWithText(
            "Ocurrió un error al cargar los coleccionistas. Por favor, comunícate con el administrador."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()

        assertEquals(1, retries)
    }
}
