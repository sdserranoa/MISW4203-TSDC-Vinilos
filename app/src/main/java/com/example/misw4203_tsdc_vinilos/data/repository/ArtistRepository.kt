package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.ArtistServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.RetrofitArtistServiceAdapter
import com.example.misw4203_tsdc_vinilos.model.ArtistDetail
import com.example.misw4203_tsdc_vinilos.model.ArtistPrizeItem
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.Prize

/**
 * Fuente única de datos de artistas (músicos y agrupaciones) para la capa de presentación.
 * Implementa el patrón Service Adapter para desacoplarse del cliente HTTP.
 */
class ArtistRepository(
    private val serviceAdapter: ArtistServiceAdapter = RetrofitArtistServiceAdapter(),
) {

    /**
     * Obtiene el detalle consolidado del artista [id]. Si [isBandHint] es nulo,
     * intenta resolverlo primero como músico y luego como banda.
     */
    suspend fun getArtistDetail(id: Int, isBandHint: Boolean? = null): ArtistDetail {
        return when (isBandHint) {
            true -> fetchBandDetail(id)
            false -> {
                try {
                    fetchMusicianDetail(id)
                } catch (e: Exception) {
                    fetchBandDetail(id)
                }
            }
            null -> {
                try {
                    fetchMusicianDetail(id)
                } catch (e: Exception) {
                    fetchBandDetail(id)
                }
            }
        }
    }

    private suspend fun fetchMusicianDetail(musicianId: Int): ArtistDetail {
        val musician = serviceAdapter.getMusician(musicianId)
        val allBands = try { serviceAdapter.getAllBands() } catch (_: Exception) { emptyList() }
        val allPrizes = try { serviceAdapter.getPrizes() } catch (_: Exception) { emptyList() }
        val allCollectors = try { serviceAdapter.getCollectors() } catch (_: Exception) { emptyList() }

        // Identificar bandas relacionadas (en las que el músico participa como integrante)
        val relatedBands = allBands.filter { band ->
            band.musicians.any { it.id == musicianId }
        }.map { band ->
            Performer(
                id = band.id,
                name = band.name,
                image = band.image,
                description = band.description,
                creationDate = band.creationDate,
            )
        }

        val prizes = musician.performerPrizes.map { pp ->
            val prizeInfo = allPrizes.firstOrNull { it.id == pp.id }
            ArtistPrizeItem(
                id = pp.id,
                name = prizeInfo?.name ?: "Reconocimiento Musical",
                organization = prizeInfo?.organization ?: "",
                premiationDate = pp.premiationDate,
                description = prizeInfo?.description ?: "",
            )
        }

        val followers = allCollectors.filter { collector ->
            collector.favoritePerformers.any { it.id == musicianId }
        }

        return ArtistDetail(
            id = musician.id,
            name = musician.name,
            image = musician.image,
            description = musician.description,
            birthDate = musician.birthDate,
            isBand = false,
            albums = musician.albums,
            relatedBands = relatedBands,
            members = emptyList(),
            prizes = prizes,
            collectors = followers,
        )
    }

    private suspend fun fetchBandDetail(bandId: Int): ArtistDetail {
        val band = serviceAdapter.getBand(bandId)
        val allPrizes = try { serviceAdapter.getPrizes() } catch (_: Exception) { emptyList() }
        val allCollectors = try { serviceAdapter.getCollectors() } catch (_: Exception) { emptyList() }

        val prizes = band.performerPrizes.map { pp ->
            val prizeInfo = allPrizes.firstOrNull { it.id == pp.id }
            ArtistPrizeItem(
                id = pp.id,
                name = prizeInfo?.name ?: "Reconocimiento Musical",
                organization = prizeInfo?.organization ?: "",
                premiationDate = pp.premiationDate,
                description = prizeInfo?.description ?: "",
            )
        }

        val followers = allCollectors.filter { collector ->
            collector.favoritePerformers.any { it.id == bandId }
        }

        return ArtistDetail(
            id = band.id,
            name = band.name,
            image = band.image,
            description = band.description,
            creationDate = band.creationDate,
            isBand = true,
            albums = band.albums,
            relatedBands = emptyList(),
            members = band.musicians,
            prizes = prizes,
            collectors = followers,
        )
    }
}
