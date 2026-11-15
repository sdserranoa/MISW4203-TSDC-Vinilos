package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/**
 * Coleccionista tal como lo exponen `GET /collectors` y `GET /collectors/{id}` en la API BackVynils.
 *
 * @property id Identificador único del coleccionista.
 * @property name Nombre completo o alias.
 * @property telephone Teléfono de contacto.
 * @property email Correo electrónico.
 * @property favoritePerformers Intérpretes favoritos que sigue el coleccionista.
 * @property comments Comentarios que el coleccionista ha publicado sobre álbumes.
 * @property collectorAlbums Álbumes que posee el coleccionista, con su precio y estado.
 */
@Serializable
data class Collector(
    val id: Int,
    val name: String,
    val telephone: String = "",
    val email: String = "",
    val favoritePerformers: List<Performer> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val collectorAlbums: List<CollectorAlbum> = emptyList(),
)

/**
 * Álbum dentro de la colección de un coleccionista. `GET /collectors` solo envía el precio y el
 * estado; `GET /collectors/{id}/albums` incluye además el [album] completo.
 *
 * @property id Identificador de la relación coleccionista-álbum.
 * @property price Precio al que el coleccionista ofrece el álbum.
 * @property status Estado del álbum en la colección (`Active` o `Inactive`).
 * @property album Álbum asociado, cuando el recurso lo incluye.
 */
@Serializable
data class CollectorAlbum(
    val id: Int,
    val price: Double = 0.0,
    val status: String = "",
    val album: Album? = null,
)
