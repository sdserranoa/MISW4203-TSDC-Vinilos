package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Collector
import retrofit2.http.GET
import retrofit2.http.Path

/** Endpoints de la API BackVynils consumidos por la aplicación. */
interface VinilosApiService {

    @GET("albums")
    suspend fun getAlbums(): List<Album>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") albumId: Int): Album

    @GET("collectors")
    suspend fun getCollectors(): List<Collector>

    @GET("collectors/{id}")
    suspend fun getCollector(@Path("id") collectorId: Int): Collector
}
