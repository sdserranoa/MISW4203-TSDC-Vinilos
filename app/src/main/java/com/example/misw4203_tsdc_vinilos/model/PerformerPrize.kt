package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/**
 * Asociación entre un intérprete y un premio recibido según la API BackVynils.
 *
 * @property id Identificador del registro.
 * @property premiationDate Fecha de premiación en formato ISO-8601 o YYYY-MM-DD.
 */
@Serializable
data class PerformerPrize(
    val id: Int = 0,
    val premiationDate: String = "",
)
