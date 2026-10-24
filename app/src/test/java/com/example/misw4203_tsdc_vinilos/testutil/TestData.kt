package com.example.misw4203_tsdc_vinilos.testutil

import com.example.misw4203_tsdc_vinilos.data.network.VinilosApiService
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Performer

/** Fixture de álbum. La carátula vacía evita que Coil realice peticiones de red durante las pruebas. */
fun testAlbum(
    id: Int,
    name: String = "Álbum $id",
    genre: String = "Rock",
    releaseDate: String = "1984-08-01T00:00:00.000Z",
    performers: List<Performer> = emptyList(),
) = Album(
    id = id,
    name = name,
    cover = "",
    releaseDate = releaseDate,
    genre = genre,
    performers = performers,
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
}
