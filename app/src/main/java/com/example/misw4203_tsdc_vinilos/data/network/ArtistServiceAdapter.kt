package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.Prize

/**
 * Adaptador de servicio para el dominio de artistas (HU03/HU04).
 * Abstrae el consumo del backend BackVynils desacoplando repositorios y ViewModels.
 */
interface ArtistServiceAdapter {
    suspend fun getMusician(id: Int): MusicianDetailResponse
    suspend fun getMusicians(): List<MusicianDetailResponse>
    suspend fun getBand(id: Int): BandDetailResponse
    suspend fun getAllBands(): List<BandDetailResponse>
    suspend fun getPrizes(): List<Prize>
    suspend fun getCollectors(): List<Collector>
}

class RetrofitArtistServiceAdapter(
    private val api: VinilosApiService = VinilosApi.service,
) : ArtistServiceAdapter {

    override suspend fun getMusician(id: Int): MusicianDetailResponse = api.getMusician(id)

    override suspend fun getMusicians(): List<MusicianDetailResponse> = api.getMusicians()

    override suspend fun getBand(id: Int): BandDetailResponse = api.getBand(id)

    override suspend fun getAllBands(): List<BandDetailResponse> = api.getBands()

    override suspend fun getPrizes(): List<Prize> = api.getPrizes()

    override suspend fun getCollectors(): List<Collector> = api.getCollectors()
}
