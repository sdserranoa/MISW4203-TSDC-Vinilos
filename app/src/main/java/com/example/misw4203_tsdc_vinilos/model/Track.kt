package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/**
 * Pista o canción de un álbum según la API BackVynils (`GET /albums/{id}`).
 *
 * @property id Identificador numérico único de la pista.
 * @property name Nombre o título de la canción.
 * @property duration Duración en formato mm:ss (por ejemplo "05:05").
 */
@Serializable
data class Track(
    val id: Int,
    val name: String,
    val duration: String = "",
)
