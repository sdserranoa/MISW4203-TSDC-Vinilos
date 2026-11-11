package com.example.misw4203_tsdc_vinilos.model

import kotlinx.serialization.Serializable

/**
 * Coleccionista registrado en el catálogo de Vinilos.
 *
 * @property id Identificador único del coleccionista.
 * @property name Nombre completo o alias.
 * @property telephone Teléfono de contacto.
 * @property email Correo electrónico.
 * @property favoritePerformers Intérpretes favoritos que sigue el coleccionista.
 */
@Serializable
data class Collector(
    val id: Int,
    val name: String,
    val telephone: String = "",
    val email: String = "",
    val favoritePerformers: List<Performer> = emptyList(),
)
