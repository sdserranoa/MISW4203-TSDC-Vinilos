package com.example.misw4203_tsdc_vinilos

import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorAlbum
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer

/**
 * Servicio HTTP falso para las pruebas E2E de coleccionistas (HU05 y HU06). Sus datos se ajustan
 * en cada prueba antes de abrir la pestaña Coleccionistas.
 */
internal class CollectorsFakeService : BaseFakeVinilosApiService() {
    var albums: List<Album> = emptyList()
    var collectors: List<Collector> = emptyList()
    var collectorAlbums: Map<Int, List<CollectorAlbum>> = emptyMap()
    var collectorsError: Exception? = null

    override suspend fun getAlbums(): List<Album> = albums

    override suspend fun getAlbum(albumId: Int): Album =
        albums.firstOrNull { it.id == albumId } ?: throw NoSuchElementException("Album $albumId not found")

    override suspend fun getCollectors(): List<Collector> {
        collectorsError?.let { throw it }
        return collectors
    }

    override suspend fun getCollector(collectorId: Int): Collector {
        collectorsError?.let { throw it }
        return collectors.firstOrNull { it.id == collectorId }
            ?: throw NoSuchElementException("Collector $collectorId not found")
    }

    override suspend fun getCollectorAlbums(collectorId: Int): List<CollectorAlbum> {
        collectorsError?.let { throw it }
        return collectorAlbums[collectorId].orEmpty()
    }

    fun reset() {
        albums = emptyList()
        collectors = emptyList()
        collectorAlbums = emptyMap()
        collectorsError = null
    }
}

/** Datos de prueba basados en los registros reales de BackVynils. */
internal object CollectorsTestData {

    val buscandoAmerica = Album(
        id = 100,
        name = "Buscando América",
        cover = "",
        releaseDate = "1984-08-01T00:00:00.000Z",
        description = "Primer álbum de Rubén Blades y Seis del Solar.",
        genre = "Salsa",
        recordLabel = "Elektra",
    )

    val nightAtTheOpera = Album(
        id = 102,
        name = "A Night at the Opera",
        cover = "",
        releaseDate = "1975-11-21T00:00:00.000Z",
        description = "Cuarto álbum de estudio de Queen.",
        genre = "Rock",
        recordLabel = "EMI",
    )

    val rubenBlades = Performer(
        id = 100,
        name = "Rubén Blades Bellido de Luna",
        birthDate = "1948-07-16T00:00:00.000Z",
    )

    val queen = Performer(
        id = 101,
        name = "Queen",
        creationDate = "1970-01-01T00:00:00.000Z",
    )

    fun collector(
        id: Int,
        name: String,
        email: String,
        telephone: String,
        albums: Int = 1,
        favoritePerformers: List<Performer> = emptyList(),
        comments: List<Comment> = emptyList(),
    ) = Collector(
        id = id,
        name = name,
        telephone = telephone,
        email = email,
        favoritePerformers = favoritePerformers,
        comments = comments,
        collectorAlbums = (1..albums).map { CollectorAlbum(id = id * 10 + it, price = 30.0, status = "Active") },
    )

    fun ownedAlbum(album: Album) = CollectorAlbum(id = album.id, price = 35.0, status = "Active", album = album)
}
