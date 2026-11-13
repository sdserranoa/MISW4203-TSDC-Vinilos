package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/** Body para asociar una nueva canción a un álbum (`POST /albums/{albumId}/tracks`). */
@Serializable
data class TrackRequest(
    val name: String,
    val duration: String,
)

/** Adaptador de servicio para el dominio de álbumes y canciones (HU01, HU02, HU08). */
interface AlbumServiceAdapter {
    suspend fun getAlbums(): List<Album>
    suspend fun getAlbum(id: Int): Album
    suspend fun postTrack(albumId: Int, track: TrackRequest): Track
}

/** Implementación del [AlbumServiceAdapter] sobre Retrofit ejecutada en [ioDispatcher]. */
class RetrofitAlbumServiceAdapter(
    private val api: VinilosApiService = VinilosApi.service,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AlbumServiceAdapter {

    override suspend fun getAlbums(): List<Album> =
        withContext(ioDispatcher) { api.getAlbums() }

    override suspend fun getAlbum(id: Int): Album =
        withContext(ioDispatcher) { api.getAlbum(id) }

    override suspend fun postTrack(albumId: Int, track: TrackRequest): Track =
        withContext(ioDispatcher) { api.postTrack(albumId, track) }
}
