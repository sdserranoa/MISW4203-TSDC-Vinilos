package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/**
 * Comentario o reseña de un álbum en la API BackVynils (`GET /albums/{id}`).
 *
 * @property id Identificador único de la reseña.
 * @property description Contenido o texto de la opinión.
 * @property rating Calificación numérica otorgada (1 a 5).
 * @property collector Datos del coleccionista autor de la reseña (opcional).
 */
@Serializable
data class Comment(
    val id: Int,
    val description: String,
    val rating: Int = 5,
    val collector: CommentCollector? = null,
)

/** Información básica del coleccionista que comentó. */
@Serializable
data class CommentCollector(
    val id: Int,
    val name: String = "",
)
