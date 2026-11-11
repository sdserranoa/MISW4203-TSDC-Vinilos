package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/**
 * Premio o galardón que puede ser otorgado a un intérprete.
 *
 * @property id Identificador del premio.
 * @property organization Entidad que otorga el galardón (p. ej. "LARAS", "Univisión").
 * @property name Nombre del premio (p. ej. "Grammy Award").
 * @property description Descripción o reseña del reconocimiento.
 */
@Serializable
data class Prize(
    val id: Int,
    val organization: String = "",
    val name: String = "",
    val description: String = "",
)
