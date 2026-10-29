package com.example.misw4203_tsdc_vinilos.testutil

import com.example.misw4203_tsdc_vinilos.data.network.VinilosApiService
import com.example.misw4203_tsdc_vinilos.model.Album
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

/** Servicio falso que responde con [albums] o lanza [error] si está definido. */
class FakeVinilosApiService(
    var albums: List<Album> = emptyList(),
    var error: Throwable? = null,
) : VinilosApiService {

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
