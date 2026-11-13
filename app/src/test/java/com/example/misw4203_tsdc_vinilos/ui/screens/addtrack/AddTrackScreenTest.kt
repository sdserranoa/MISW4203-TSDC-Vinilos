package com.example.misw4203_tsdc_vinilos.ui.screens.addtrack

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36], qualifiers = "w411dp-h891dp-xxhdpi")
class AddTrackScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val abbeyRoad = testAlbum(1, name = "Abbey Road")

    private fun formState(
        name: String = "",
        duration: String = "",
    ) = AddTrackUiState.Form(album = abbeyRoad, name = name, duration = duration)

    private fun setContent(
        uiState: AddTrackUiState,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
        onNameChange: (String) -> Unit = {},
        onDurationChange: (String) -> Unit = {},
        onSubmit: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                AddTrackContent(
                    uiState = uiState,
                    onBack = onBack,
                    onRetry = onRetry,
                    onNameChange = onNameChange,
                    onDurationChange = onDurationChange,
                    onSubmit = onSubmit,
                )
            }
        }
    }

    @Test
    fun formulario_muestraInformacionDelAlbumYCampos() {
        setContent(formState())

        composeTestRule.onNodeWithText("Agregar Canción").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abbey Road").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nombre de la canción *").assertIsDisplayed()
        composeTestRule.onNodeWithText("Duración *").assertIsDisplayed()
        composeTestRule.onNodeWithText("+ Asociar Canción").assertIsNotEnabled()
    }

    @Test
    fun formulario_camposCompletos_habilitaBotonAsociar() {
        setContent(formState(name = "Come Together", duration = "04:20"))

        composeTestRule.onNodeWithText("+ Asociar Canción").assertIsEnabled()
    }

    @Test
    fun formulario_alCancelar_invocaAccionDeNavegacion() {
        var backClicked = false
        setContent(formState(), onBack = { backClicked = true })

        composeTestRule.onNodeWithText("Cancelar").performClick()

        assertEquals(true, backClicked)
    }

    @Test
    fun entradaNombre_alEscribir_notificaCambio() {
        var enteredName = ""
        setContent(formState(), onNameChange = { enteredName = it })

        composeTestRule.onNodeWithText("Come Together").performTextInput("Decisiones")

        assertEquals("Decisiones", enteredName)
    }
}
