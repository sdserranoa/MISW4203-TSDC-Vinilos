package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/**
 * Álbum tal como lo expone el recurso `GET /albums` de la API BackVynils.
 * La fecha de lanzamiento llega en formato ISO-8601 (por ejemplo `1984-08-01T00:00:00.000Z`).
 */
@Serializable
data class Album(
    // Con valor por defecto 0 el id no se serializa al crear: BackVynils rechaza claves desconocidas.
    val id: Int = 0,
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String = "",
    val genre: String,
    val recordLabel: String = "",
    val performers: List<Performer> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val comments: List<Comment> = emptyList(),
) {
    val releaseYear: String
        get() = releaseDate.take(4)
}
