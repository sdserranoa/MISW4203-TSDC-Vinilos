package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.VinilosApi
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApiService
import com.example.misw4203_tsdc_vinilos.model.Album

/** Fuente única de datos de álbumes para la capa de presentación. */
class AlbumRepository(
    private val apiService: VinilosApiService = VinilosApi.service,
) {

    suspend fun getAlbums(): List<Album> = apiService.getAlbums()
}
