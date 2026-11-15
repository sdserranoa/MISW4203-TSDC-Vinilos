package com.example.misw4203_tsdc_vinilos.model

import androidx.compose.runtime.Immutable

private val WIKIMEDIA_THUMB_REGEX = Regex("/thumb/(.+)/[^/]+$")

/**
 * Modelo de dominio para la pantalla de detalle de un artista o agrupación (HU04).
 * Marcado con [@Immutable] para garantizar a Compose que las propiedades de lista
 * no mutarán, permitiendo a los composables omitir (skip) recomposiciones innecesarias.
 *
 * @property id Identificador único.
 * @property name Nombre artístico o de la agrupación.
 * @property image URL del retrato o portada.
 * @property description Biografía o reseña histórica.
 * @property birthDate Fecha de nacimiento (si es solista).
 * @property creationDate Fecha de conformación (si es banda).
 * @property isBand Indica si la entidad representa una banda musical.
 * @property albums Álbumes asociados en los que participó.
 * @property relatedBands Bandas musicales a las que pertenece el músico (si es solista).
 * @property members Músicos que integran la agrupación (si es banda).
 * @property prizes Premios y galardones reconocidos.
 * @property collectors Coleccionistas que tienen al artista en sus favoritos.
 */
@Immutable
data class ArtistDetail(
    val id: Int,
    val name: String,
    val image: String = "",
    val description: String = "",
    val birthDate: String = "",
    val creationDate: String = "",
    val isBand: Boolean = creationDate.isNotBlank(),
    val albums: List<Album> = emptyList(),
    val relatedBands: List<Performer> = emptyList(),
    val members: List<Performer> = emptyList(),
    val prizes: List<ArtistPrizeItem> = emptyList(),
    val collectors: List<Collector> = emptyList(),
) {
    /**
     * URL sanitizada de la imagen calculada una sola vez al instanciar el modelo,
     * evitando la recompilación de expresiones regulares y re-asignaciones en memoria.
     */
    val normalizedImage: String = if (image.contains("upload.wikimedia.org") && image.contains("/thumb/")) {
        image.replace(WIKIMEDIA_THUMB_REGEX, "/$1")
    } else {
        image
    }

    val dateLabel: String = if (isBand) "Creación" else "Nacimiento"

    /** Fecha formateada a nivel de año-mes-día precalculada para evitar substrings en cada frame. */
    val formattedDate: String = (if (isBand) creationDate else birthDate).take(10)
}

/**
 * Premio proyectado para visualización en la ficha del artista.
 * Marcado con [@Immutable] para permitir saltos de recomposición en [ArtistAwardsSection].
 */
@Immutable
data class ArtistPrizeItem(
    val id: Int,
    val name: String,
    val organization: String,
    val premiationDate: String,
    val description: String = "",
) {
    val year: String = premiationDate.take(4).ifBlank { "" }
}
