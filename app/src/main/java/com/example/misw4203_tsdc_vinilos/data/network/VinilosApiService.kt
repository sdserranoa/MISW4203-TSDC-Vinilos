package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Album
import retrofit2.http.GET

/** Endpoints de la API BackVynils consumidos por la aplicación. */
interface VinilosApiService {

    @GET("albums")
    suspend fun getAlbums(): List<Album>
}
