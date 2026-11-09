package com.example.misw4203_tsdc_vinilos.model

/**
 * Información que muestra el detalle de un coleccionista (HU06): sus datos de contacto, los
 * álbumes que posee, sus artistas favoritos y los comentarios que ha publicado.
 *
 * @property albums Álbumes de su colección, con nombre, carátula, género y fecha de lanzamiento.
 * @property favoritePerformers Músicos y bandas que el coleccionista sigue.
 * @property comments Comentarios del coleccionista sobre álbumes.
 */
data class CollectorDetail(
    val id: Int,
    val name: String,
    val telephone: String,
    val email: String,
    val albums: List<Album>,
    val favoritePerformers: List<Performer>,
    val comments: List<Comment>,
) {
    /** Iniciales para el avatar; se calculan una sola vez al crear el detalle. */
    val initials: String = initialsOf(name)
}

/**
 * Une el coleccionista de `GET /collectors/{id}` con los álbumes de `GET /collectors/{id}/albums`.
 * Se descartan las relaciones que llegan sin el álbum completo y los registros repetidos, porque
 * cada elemento de las listas de la pantalla se identifica por su id.
 */
fun Collector.toDetail(collectorAlbums: List<CollectorAlbum>): CollectorDetail = CollectorDetail(
    id = id,
    name = name,
    telephone = telephone,
    email = email,
    albums = collectorAlbums.mapNotNull { it.album }.distinctBy { it.id },
    favoritePerformers = favoritePerformers.distinctBy { it.id },
    comments = comments.distinctBy { it.id },
)
