package com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.CommentCollector
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.testutil.testPerformer
import com.example.misw4203_tsdc_vinilos.testutil.testTrack
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de interfaz en Jetpack Compose del detail de álbum (HU02), ejecutadas en la JVM
 * con Robolectric sobre una pantalla de teléfono de 411 x 891 dp.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class AlbumDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        uiState: AlbumDetailUiState,
        onBackPage: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                AlbumDetailContent(
                    uiState = uiState,
                    onBack = onBackPage,
                    onRetry = onRetry,
                )
            }
        }
    }

    @Test
    fun exito_muestraInformacionDelAlbum() {
        setContent(
            AlbumDetailUiState.Success(
                album = testAlbum(
                    1,
                    "Buscando América",
                    "Salsa",
                    "1984-08-01T00:00:00.000Z",
                    performers = listOf(
                        testPerformer(1)
                    ),
                    tracks = listOf(
                        testTrack(1),
                        testTrack(2),
                        testTrack(3),
                    )
                )
            )
        )

        composeTestRule.onNodeWithText("Detalle de Álbum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buscando América").assertIsDisplayed()
        composeTestRule.onNodeWithText("Salsa").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Carátula del álbum Buscando América")
            .assertIsDisplayed()
        listOf("Buscando América", "Salsa", "1984", "Descripción del álbum 1")
            .forEach { text ->
                composeTestRule.onNodeWithText(text).performScrollTo().assertIsDisplayed()
            }
        composeTestRule.onAllNodesWithText("EMI").onFirst().performScrollTo().assertIsDisplayed()
    }

    @Test
    fun canciones_muestraTodasLasPistasYSusDuraciones() {
        setContent(
            AlbumDetailUiState.Success(
                testAlbum(
                    id = 1,
                    tracks = listOf(
                        testTrack(1, duration = "3:15"),
                        testTrack(2, duration = "4:30"),
                        testTrack(3, duration = "5:05"),
                    ),
                )
            )
        )

        listOf("Canción 1", "3:15", "Canción 2", "4:30", "Canción 3", "5:05")
            .forEach { text ->
                composeTestRule.onNodeWithText(text).performScrollTo().assertIsDisplayed()
            }
        composeTestRule.onNodeWithText("Este álbum aún no tiene pistas registradas en el catálogo.")
            .assertDoesNotExist()
    }

    @Test
    fun interpretes_muestraLosArtistasAlCambiarDePestana() {
        setContent(
            AlbumDetailUiState.Success(
                testAlbum(
                    id = 1,
                    performers = listOf(
                        testPerformer(1, name = "Carlos Vives"),
                        testPerformer(2, name = "Rubén Blades"),
                    ),
                )
            )
        )

        composeTestRule.onNodeWithText("Intérpretes").performScrollTo().performClick()

        listOf("Carlos Vives", "Rubén Blades", "Descripción del interprete 1", "Descripción del interprete 2")
            .forEach { text ->
                composeTestRule.onNodeWithText(text).performScrollTo().assertIsDisplayed()
            }
    }

    @Test
    fun comentarios_muestraCalificacionDescripcionYColeccionista() {
        setContent(
            AlbumDetailUiState.Success(
                testAlbum(
                    id = 1,
                    comments = listOf(
                        Comment(
                            id = 1,
                            description = "Excelente álbum",
                            rating = 4,
                            collector = CommentCollector(id = 1, name = "Ana Pérez"),
                        ),
                        Comment(
                            id = 2,
                            description = "Un clásico",
                            rating = 5,
                            collector = CommentCollector(id = 2, name = "Luis Torres"),
                        ),
                    ),
                )
            )
        )

        composeTestRule.onNodeWithText("Comentarios").performScrollTo().performClick()

        listOf("Ana Pérez", "\"Excelente álbum\"", "★★★★☆", "Luis Torres", "\"Un clásico\"", "★★★★★")
            .forEach { text ->
                composeTestRule.onNodeWithText(text).performScrollTo().assertIsDisplayed()
            }
        composeTestRule.onNodeWithText("Aún no hay comentarios ni reseñas para este álbum.")
            .assertDoesNotExist()
    }

    @Test
    fun comentarioSinAutor_muestraNombrePredeterminado() {
        setContent(
            AlbumDetailUiState.Success(
                testAlbum(
                    id = 1,
                    comments = listOf(Comment(id = 1, description = "Excelente álbum")),
                )
            )
        )

        composeTestRule.onNodeWithText("Comentarios").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Coleccionista").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun sinCanciones_muestraMensajeDeNoDisponibles() {
        setContent(AlbumDetailUiState.Success(testAlbum(id = 1)))

        composeTestRule.onNodeWithText("Este álbum aún no tiene pistas registradas en el catálogo.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun sinInterpretes_muestraMensajeDeNoDisponibles() {
        setContent(AlbumDetailUiState.Success(testAlbum(id = 1)))

        composeTestRule.onNodeWithText("Intérpretes").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Este álbum no tiene artistas ni músicos asociados.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun sinComentarios_muestraMensajeDeNoDisponibles() {
        setContent(AlbumDetailUiState.Success(testAlbum(id = 1)))

        composeTestRule.onNodeWithText("Comentarios").performScrollTo().performClick()

        composeTestRule.onNodeWithText("Aún no hay comentarios ni reseñas para este álbum.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun cargando_muestraMensajeDeCarga() {
        setContent(AlbumDetailUiState.Loading)

        composeTestRule.onNodeWithText("Cargando detalle del álbum…").assertIsDisplayed()
    }

    @Test
    fun error_muestraMensajeParaContactarAlAdministradorYPermiteReintentar() {
        var retries = 0
        setContent(AlbumDetailUiState.Error, onRetry = { retries++ })

        composeTestRule.onNodeWithText(
            "Ocurrió un error al cargar el álbum. Por favor, comunícate con el administrador."
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun volver_ejecutaCallback() {
        var backClicks = 0
        setContent(
            AlbumDetailUiState.Success(testAlbum(id = 1)),
            onBackPage = { backClicks++ },
        )

        composeTestRule.onNodeWithContentDescription("Volver al catálogo").performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun descripcionLarga_permiteExpandirYColapsar() {
        val longDescription = "Esta es una descripción muy detallada que supera los ciento cuarenta caracteres requeridos para desplegar el botón interactivo de ver más detalles sobre la obra musical y su contexto histórico."
        val album = testAlbum(
            id = 100,
            name = "Álbum con descripción larga",
            description = longDescription,
        )

        setContent(AlbumDetailUiState.Success(album))

        composeTestRule.onNodeWithText("Leer más").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Leer más").performClick()
        composeTestRule.onNodeWithText("Leer menos").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Leer menos").performClick()
        composeTestRule.onNodeWithText("Leer más").performScrollTo().assertIsDisplayed()
    }
}