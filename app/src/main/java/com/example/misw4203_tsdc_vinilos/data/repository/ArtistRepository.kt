package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.ArtistServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.RetrofitArtistServiceAdapter
import com.example.misw4203_tsdc_vinilos.model.ArtistDetail
import com.example.misw4203_tsdc_vinilos.model.ArtistPrizeItem
import com.example.misw4203_tsdc_vinilos.model.Performer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Fuente única de datos de artistas (músicos y agrupaciones) para la capa de presentación.
 * Implementa el patrón Service Adapter y optimiza las peticiones de red mediante corrutinas
 * concurrentes (async/await) y despacho en el hilo de E/S ([Dispatchers.IO]).
 *
 * Micro-optimizaciones aplicadas:
 * - Resolución de premios indexada $O(1)$ vía mapa hash en vez de búsqueda lineal $O(N \times M)$.
 * - Proyección directa de bandas relacionadas vía [mapNotNull] evitando asignaciones intermedias de colecciones.
 * - Despacho asíncrono no bloqueante con [withContext] y [async] en paralelo.
 *
 * @property serviceAdapter Adaptador de red hacia la API de Vinilos.
 * @property ioDispatcher Despachador de corrutinas para operaciones de red (por defecto [Dispatchers.IO]).
 */
class ArtistRepository @JvmOverloads constructor(
    private val serviceAdapter: ArtistServiceAdapter = RetrofitArtistServiceAdapter(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /**
     * Obtiene el detalle consolidado del artista [id]. Si [isBandHint] es nulo,
     * intenta resolverlo primero como músico y luego como banda.
     */
    suspend fun getArtistDetail(id: Int, isBandHint: Boolean? = null): ArtistDetail = withContext(ioDispatcher) {
        when (isBandHint) {
            true -> fetchBandDetail(id)
            false -> {
                try {
                    fetchMusicianDetail(id)
                } catch (_: Exception) {
                    fetchBandDetail(id)
                }
            }
            null -> {
                try {
                    fetchMusicianDetail(id)
                } catch (_: Exception) {
                    fetchBandDetail(id)
                }
            }
        }
    }

    /**
     * Consulta el músico y recupera en paralelo (con [async]) las bandas relacionadas,
     * los premios y los coleccionistas para minimizar la latencia percibida por el usuario.
     */
    private suspend fun fetchMusicianDetail(musicianId: Int): ArtistDetail = coroutineScope {
        // Lanzamiento concurrente de las 4 peticiones a la API
        val musicianDeferred = async { serviceAdapter.getMusician(musicianId) }
        val allBandsDeferred = async {
            try { serviceAdapter.getAllBands() } catch (_: Exception) { emptyList() }
        }
        val allPrizesDeferred = async {
            try { serviceAdapter.getPrizes() } catch (_: Exception) { emptyList() }
        }
        val allCollectorsDeferred = async {
            try { serviceAdapter.getCollectors() } catch (_: Exception) { emptyList() }
        }

        // Espera de resultados concurrentes
        val musician = musicianDeferred.await()
        val allBands = allBandsDeferred.await()
        val allPrizes = allPrizesDeferred.await()
        val allCollectors = allCollectorsDeferred.await()

        // Micro-optimización: Proyección en un solo paso con mapNotNull (elimina lista intermedia)
        val relatedBands = if (allBands.isEmpty()) {
            emptyList()
        } else {
            allBands.mapNotNull { band ->
                if (band.musicians.any { it.id == musicianId }) {
                    Performer(
                        id = band.id,
                        name = band.name,
                        image = band.image,
                        description = band.description,
                        creationDate = band.creationDate,
                    )
                } else null
            }
        }

        // Micro-optimización: Indexación O(1) de premios mediante AssociateBy
        val prizesById = if (allPrizes.isEmpty()) emptyMap() else allPrizes.associateBy { it.id }
        val prizes = musician.performerPrizes.map { pp ->
            val prizeInfo = prizesById[pp.id]
            ArtistPrizeItem(
                id = pp.id,
                name = prizeInfo?.name ?: "Reconocimiento Musical",
                organization = prizeInfo?.organization ?: "",
                premiationDate = pp.premiationDate,
                description = prizeInfo?.description ?: "",
            )
        }

        val followers = if (allCollectors.isEmpty()) {
            emptyList()
        } else {
            allCollectors.filter { collector ->
                collector.favoritePerformers.any { it.id == musicianId }
            }
        }

        ArtistDetail(
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

    /**
     * Consulta la banda y recupera en paralelo (con [async]) premios y coleccionistas.
     */
    private suspend fun fetchBandDetail(bandId: Int): ArtistDetail = coroutineScope {
        val bandDeferred = async { serviceAdapter.getBand(bandId) }
        val allPrizesDeferred = async {
            try { serviceAdapter.getPrizes() } catch (_: Exception) { emptyList() }
        }
        val allCollectorsDeferred = async {
            try { serviceAdapter.getCollectors() } catch (_: Exception) { emptyList() }
        }

        val band = bandDeferred.await()
        val allPrizes = allPrizesDeferred.await()
        val allCollectors = allCollectorsDeferred.await()

        // Micro-optimización: Indexación O(1) de premios
        val prizesById = if (allPrizes.isEmpty()) emptyMap() else allPrizes.associateBy { it.id }
        val prizes = band.performerPrizes.map { pp ->
            val prizeInfo = prizesById[pp.id]
            ArtistPrizeItem(
                id = pp.id,
                name = prizeInfo?.name ?: "Reconocimiento Musical",
                organization = prizeInfo?.organization ?: "",
                premiationDate = pp.premiationDate,
                description = prizeInfo?.description ?: "",
            )
        }

        val followers = if (allCollectors.isEmpty()) {
            emptyList()
        } else {
            allCollectors.filter { collector ->
                collector.favoritePerformers.any { it.id == bandId }
            }
        }

        ArtistDetail(
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
