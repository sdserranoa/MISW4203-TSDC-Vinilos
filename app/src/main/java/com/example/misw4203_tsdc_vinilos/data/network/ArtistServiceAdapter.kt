package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.Prize
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArtistServiceAdapter {

    override suspend fun getMusician(id: Int): MusicianDetailResponse =
        withContext(ioDispatcher) { api.getMusician(id) }

    override suspend fun getMusicians(): List<MusicianDetailResponse> =
        withContext(ioDispatcher) { api.getMusicians() }

    override suspend fun getBand(id: Int): BandDetailResponse =
        withContext(ioDispatcher) { api.getBand(id) }

    override suspend fun getAllBands(): List<BandDetailResponse> =
        withContext(ioDispatcher) { api.getBands() }

    override suspend fun getPrizes(): List<Prize> =
        withContext(ioDispatcher) { api.getPrizes() }

    override suspend fun getCollectors(): List<Collector> =
        withContext(ioDispatcher) { api.getCollectors() }
}
