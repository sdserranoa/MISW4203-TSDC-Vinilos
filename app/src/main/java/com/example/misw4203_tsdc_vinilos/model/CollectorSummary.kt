package com.example.misw4203_tsdc_vinilos.model

/**
 * Vista resumida de un coleccionista para el listado (HU05). Conserva solo los datos que muestra
 * la tarjeta, de modo que los comentarios e intérpretes anidados de la respuesta de la API no
 * permanecen en memoria mientras el usuario navega por el listado.
 *
 * @property id Identificador único del coleccionista.
 * @property name Nombre completo o alias.
 * @property email Correo electrónico.
 * @property albumCount Cantidad de álbumes que tiene en su colección.
 */
data class CollectorSummary(
    val id: Int,
    val name: String,
    val email: String,
    val albumCount: Int,
) {
    /** Iniciales para el avatar; se calculan una sola vez al crear el resumen. */
    val initials: String = initialsOf(name)
}

/** Expresión regular compilada una sola vez y reutilizada en cada cálculo de iniciales. */
private val WHITESPACE = Regex("\\s+")

/** Hasta dos iniciales en mayúscula a partir de [name] (por ejemplo, «Manolo Bellon» → «MB»). */
fun initialsOf(name: String): String = name.trim()
    .split(WHITESPACE)
    .filter { it.isNotEmpty() }
    .take(2)
    .joinToString(separator = "") { it.first().uppercase() }

/** Convierte el coleccionista completo de la API en el resumen que usa el listado. */
fun Collector.toSummary(): CollectorSummary = CollectorSummary(
    id = id,
    name = name,
    email = email,
    albumCount = collectorAlbums.size,
)
