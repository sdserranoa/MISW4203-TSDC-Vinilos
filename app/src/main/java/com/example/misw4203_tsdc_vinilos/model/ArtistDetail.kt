package com.example.misw4203_tsdc_vinilos.model

/**
 * Modelo de dominio para la pantalla de detalle de un artista o agrupación (HU04).
 * Unifica tanto a músicos solistas como a agrupaciones musicales (bandas), incorporando
 * álbumes asociados, premios, coleccionistas que lo siguen y relaciones de bandas/miembros.
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
     * Sanitiza URLs de miniaturas problemáticas de Wikimedia Commons para obtener la imagen
     * original en alta fidelidad y evitar errores HTTP 400.
     */
    val normalizedImage: String
        get() {
            if (image.contains("upload.wikimedia.org") && image.contains("/thumb/")) {
                return image.replace(Regex("/thumb/(.+)/[^/]+$"), "/$1")
            }
            return image
        }

    val dateLabel: String
        get() = if (isBand) "Creación" else "Nacimiento"

    val formattedDate: String
        get() {
            val raw = if (isBand) creationDate else birthDate
            if (raw.isBlank()) return ""
            return raw.take(10)
        }
}

/**
 * Premio proyectado para visualización en la ficha del artista.
 */
data class ArtistPrizeItem(
    val id: Int,
    val name: String,
    val organization: String,
    val premiationDate: String,
    val description: String = "",
) {
    val year: String
        get() = premiationDate.take(4).ifBlank { "" }
}
