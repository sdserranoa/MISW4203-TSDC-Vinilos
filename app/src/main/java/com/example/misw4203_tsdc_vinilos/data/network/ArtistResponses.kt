package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.PerformerPrize
import kotlinx.serialization.Serializable

/**
 * Respuesta JSON expuesta por `GET /musicians/{id}` y `GET /musicians`.
 */
@Serializable
data class MusicianDetailResponse(
    val id: Int,
    val name: String,
    val image: String = "",
    val description: String = "",
    val birthDate: String = "",
    val albums: List<Album> = emptyList(),
    val performerPrizes: List<PerformerPrize> = emptyList(),
)

/**
 * Respuesta JSON expuesta por `GET /bands/{id}` y `GET /bands`.
 */
@Serializable
data class BandDetailResponse(
    val id: Int,
    val name: String,
    val image: String = "",
    val description: String = "",
    val creationDate: String = "",
    val albums: List<Album> = emptyList(),
    val musicians: List<Performer> = emptyList(),
    val performerPrizes: List<PerformerPrize> = emptyList(),
)
