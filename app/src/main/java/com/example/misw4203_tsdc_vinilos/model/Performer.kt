package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/**
 * Intérprete (músico o banda) asociado a un álbum según la API BackVynils.
 *
 * @property id Identificador único del intérprete.
 * @property name Nombre artístico o de la agrupación.
 * @property image URL de la fotografía o retrato.
 * @property description Biografía o reseña del intérprete.
 * @property birthDate Fecha de nacimiento (si es solista).
 * @property creationDate Fecha de conformación (si es banda).
 */
@Serializable
data class Performer(
    val id: Int,
    val name: String,
    val image: String = "",
    val description: String = "",
    val birthDate: String = "",
    val creationDate: String = "",
) {
    val isBand: Boolean
        get() = creationDate.isNotBlank()

    val performerType: String
        get() = if (isBand) "Banda / Agrupación" else "Músico / Solista"

    /**
     * Sanitiza URLs de Wikimedia Commons que usan miniaturas arbitrarias (`/thumb/.../800px-...`)
     * las cuales devuelven HTTP 400 por políticas recientes de Wikimedia. Devuelve la URL canónica
     * directa de la imagen en máxima fidelidad.
     */
    val normalizedImage: String
        get() {
            if (image.contains("upload.wikimedia.org") && image.contains("/thumb/")) {
                return image.replace(Regex("/thumb/(.+)/[^/]+$"), "/$1")
            }
            return image
        }
}
