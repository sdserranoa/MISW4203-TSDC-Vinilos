package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorAlbum
import com.example.misw4203_tsdc_vinilos.model.Prize
import retrofit2.http.GET
import retrofit2.http.Path

/** Endpoints de la API BackVynils consumidos por la aplicación. */
interface VinilosApiService {

    @GET("albums")
    suspend fun getAlbums(): List<Album>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") albumId: Int): Album

    @GET("musicians")
    suspend fun getMusicians(): List<MusicianDetailResponse>

    @GET("musicians/{id}")
    suspend fun getMusician(@Path("id") musicianId: Int): MusicianDetailResponse

    @GET("bands")
    suspend fun getBands(): List<BandDetailResponse>

    @GET("bands/{id}")
    suspend fun getBand(@Path("id") bandId: Int): BandDetailResponse

    @GET("prizes")
    suspend fun getPrizes(): List<Prize>

    @GET("collectors")
    suspend fun getCollectors(): List<Collector>

    @GET("collectors/{id}")
    suspend fun getCollector(@Path("id") collectorId: Int): Collector

    @GET("collectors/{id}/albums")
    suspend fun getCollectorAlbums(@Path("id") collectorId: Int): List<CollectorAlbum>
}
