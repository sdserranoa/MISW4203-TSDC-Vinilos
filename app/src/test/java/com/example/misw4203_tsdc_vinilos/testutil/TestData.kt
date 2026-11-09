package com.example.misw4203_tsdc_vinilos.testutil

import com.example.misw4203_tsdc_vinilos.data.network.VinilosApiService
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorAlbum
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.Track

/** Fixture de álbum. La carátula vacía evita que Coil realice peticiones de red durante las pruebas. */
fun testAlbum(
    id: Int,
    name: String = "Álbum $id",
    genre: String = "Rock",
    releaseDate: String = "1984-08-01T00:00:00.000Z",
    performers: List<Performer> = emptyList(),
    description: String = "Descripción del álbum $id",
    recordLabel: String = "EMI",
    tracks: List<Track> = emptyList(),
    comments: List<Comment> = emptyList(),
) = Album(
    id = id,
    name = name,
    cover = "",
    releaseDate = releaseDate,
    description = description,
    genre = genre,
    recordLabel = recordLabel,
    performers = performers,
    tracks = tracks,
    comments = comments
)

fun testPerformer(
    id: Int,
    name: String = "Carlos Vives",
    description: String = "Descripción del interprete $id",
    birthDate: String = "1984-08-01T00:00:00.000Z",
    creationDate: String = "2026-08-01T00:00:00.000Z",
) = Performer(
    id = id,
    name = name,
    image = "",
    description = description,
    birthDate = birthDate,
    creationDate = creationDate,
)

fun testTrack(
    id: Int,
    name: String = "Canción $id",
    duration: String = "4:30",
) = Track(
    id = id,
    name = name,
    duration = duration,
)

fun testAlbums(count: Int): List<Album> = (1..count).map { testAlbum(id = it) }

/** Fixture de coleccionista con [albumCount] álbumes en su colección. */
fun testCollector(
    id: Int,
    name: String = "Coleccionista $id",
    email: String = "coleccionista$id@vinilos.co",
    telephone: String = "300000$id",
    albumCount: Int = 1,
    favoritePerformers: List<Performer> = emptyList(),
    comments: List<Comment> = emptyList(),
) = Collector(
    id = id,
    name = name,
    telephone = telephone,
    email = email,
    favoritePerformers = favoritePerformers,
    comments = comments,
    collectorAlbums = (1..albumCount).map { CollectorAlbum(id = id * 100 + it, price = 25.0, status = "Active") },
)

fun testCollectors(count: Int): List<Collector> = (1..count).map { testCollector(id = it) }

/** Servicio falso que responde con [albums] y [collectors], o lanza [error] si está definido. */
class FakeVinilosApiService(
    var albums: List<Album> = emptyList(),
    var error: Throwable? = null,
    var collectors: List<Collector> = emptyList(),
) : VinilosApiService {

    override suspend fun getCollectors(): List<Collector> {
        error?.let { throw it }
        return collectors
    }

    override suspend fun getCollector(collectorId: Int): Collector {
        error?.let { throw it }
        return collectors.firstOrNull { it.id == collectorId }
            ?: throw NoSuchElementException("Collector with id $collectorId not found")
    }

    override suspend fun getAlbums(): List<Album> {
        error?.let { throw it }
        return albums
    }

    override suspend fun getAlbum(albumId: Int): Album {
        error?.let { throw it }
        return albums.firstOrNull { it.id == albumId }
            ?: throw NoSuchElementException("Album with id $albumId not found")
    }
}
