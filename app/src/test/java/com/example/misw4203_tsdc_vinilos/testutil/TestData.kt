package com.example.misw4203_tsdc_vinilos.testutil

import com.example.misw4203_tsdc_vinilos.data.network.BandDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.MusicianDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApiService
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.ArtistDetail
import com.example.misw4203_tsdc_vinilos.model.ArtistPrizeItem
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorAlbum
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.Prize
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

fun testArtistDetail(
    id: Int,
    name: String = "Gustavo Cerati",
    description: String = "Cantautor, guitarrista y compositor argentino.",
    birthDate: String = "1959-08-11",
    creationDate: String = "",
    isBand: Boolean = false,
    albums: List<Album> = emptyList(),
    relatedBands: List<Performer> = emptyList(),
    members: List<Performer> = emptyList(),
    prizes: List<ArtistPrizeItem> = emptyList(),
    collectors: List<Collector> = emptyList(),
) = ArtistDetail(
    id = id,
    name = name,
    image = "",
    description = description,
    birthDate = birthDate,
    creationDate = creationDate,
    isBand = isBand,
    albums = albums,
    relatedBands = relatedBands,
    members = members,
    prizes = prizes,
    collectors = collectors,
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

/** Relación coleccionista-álbum como la devuelve `GET /collectors/{id}/albums`. */
fun testCollectorAlbum(album: Album, id: Int = album.id) =
    CollectorAlbum(id = id, price = 25.0, status = "Active", album = album)

/**
 * Servicio falso que responde con datos de prueba, incluidos los álbumes de cada coleccionista en
 * [collectorAlbums] (por id de coleccionista), o lanza [error] si está definido.
 */
class FakeVinilosApiService(
    var albums: List<Album> = emptyList(),
    var musicians: List<MusicianDetailResponse> = emptyList(),
    var bands: List<BandDetailResponse> = emptyList(),
    var prizes: List<Prize> = emptyList(),
    var collectors: List<Collector> = emptyList(),
    var error: Throwable? = null,
    var postTrackError: Throwable? = null,
    var collectorAlbums: Map<Int, List<CollectorAlbum>> = emptyMap(),
) : VinilosApiService, com.example.misw4203_tsdc_vinilos.data.network.AlbumServiceAdapter {

    override suspend fun getCollectorAlbums(collectorId: Int): List<CollectorAlbum> {
        error?.let { throw it }
        if (collectors.none { it.id == collectorId }) {
            throw NoSuchElementException("Collector with id $collectorId not found")
        }
        return collectorAlbums[collectorId].orEmpty()
    }

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

    override suspend fun createAlbum(album: Album): Album {
        (postTrackError ?: error)?.let { throw it }
        val created = if (album.id == 0) album.copy(id = (albums.maxOfOrNull { it.id } ?: 0) + 1) else album
        albums = albums + created
        return created
    }

    override suspend fun getMusicians(): List<MusicianDetailResponse> {
        error?.let { throw it }
        return musicians
    }

    override suspend fun getMusician(musicianId: Int): MusicianDetailResponse {
        error?.let { throw it }
        return musicians.firstOrNull { it.id == musicianId }
            ?: throw NoSuchElementException("Musician with id $musicianId not found")
    }

    override suspend fun getBands(): List<BandDetailResponse> {
        error?.let { throw it }
        return bands
    }

    override suspend fun getBand(bandId: Int): BandDetailResponse {
        error?.let { throw it }
        return bands.firstOrNull { it.id == bandId }
            ?: throw NoSuchElementException("Band with id $bandId not found")
    }

    override suspend fun getPrizes(): List<Prize> {
        error?.let { throw it }
        return prizes
    }

    override suspend fun postTrack(albumId: Int, track: com.example.misw4203_tsdc_vinilos.data.network.TrackRequest): Track {
        (postTrackError ?: error)?.let { throw it }
        val newTrack = Track(id = (100..999).random(), name = track.name, duration = track.duration)
        albums = albums.map { album ->
            if (album.id == albumId) {
                album.copy(tracks = album.tracks + newTrack)
            } else album
        }
        return newTrack
    }
}
