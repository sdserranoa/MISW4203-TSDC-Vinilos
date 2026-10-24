package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/** Intérprete (músico o banda) asociado a un álbum. */
@Serializable
data class Performer(
    val id: Int,
    val name: String,
)
