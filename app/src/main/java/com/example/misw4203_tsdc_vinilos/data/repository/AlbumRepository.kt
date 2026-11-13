package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.AlbumServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.RetrofitAlbumServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.TrackRequest
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Track

/** Fuente única de datos de álbumes para la capa de presentación. */
class AlbumRepository(
    private val serviceAdapter: AlbumServiceAdapter = RetrofitAlbumServiceAdapter(),
) {

    suspend fun getAlbums(): List<Album> = serviceAdapter.getAlbums()

    suspend fun getAlbum(id: Int): Album {
        return try {
            serviceAdapter.getAlbum(id)
        } catch (e: Exception) {
            serviceAdapter.getAlbums().firstOrNull { it.id == id } ?: throw e
        }
    }

    suspend fun addTrackToAlbum(albumId: Int, name: String, duration: String): Track {
        val request = TrackRequest(name = name, duration = duration)
        return serviceAdapter.postTrack(albumId, request)
    }
}
