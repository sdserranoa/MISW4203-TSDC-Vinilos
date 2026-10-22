package com.example.misw4203_tsdc_vinilos

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pruebas de interfaz de usuario de Jetpack Compose ejecutadas en la JVM con Robolectric.
 * Se ejecutan directamente en la máquina virtual en pocos segundos sin necesidad de emulador.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GreetingComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greeting_muestraTextoInicialYBoton() {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                Greeting(name = "Android")
            }
        }

        // Verifica que el saludo inicial y el botón estén visibles
        composeTestRule.onNodeWithText("Hello Android!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explorar Catalogo").assertIsDisplayed()
    }

    @Test
    fun greeting_alHacerClicEnBoton_actualizaTextoYEstado() {
        composeTestRule.setContent {
            MISW4203TSDCVinilosTheme {
                Greeting(name = "Coleccionista")
            }
        }

        // Ejecuta clic en el botón de Jetpack Compose
        composeTestRule.onNodeWithText("Explorar Catalogo").performClick()

        // Valida la recomposición con el nuevo texto y estado del botón
        composeTestRule.onNodeWithText("Bienvenido a Vinilos, Coleccionista!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ocultar").assertIsDisplayed()
    }
}
