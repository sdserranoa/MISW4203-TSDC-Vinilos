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
 * concurrentes (async/await).
 *
 * Micro-optimizaciones aplicadas:
 * - Las peticiones remotas se ejecutan en el adaptador ([Dispatchers.IO]) y las transformaciones
 *   de datos se realizan fuera del hilo principal ([defaultDispatcher]).
 * - Resolución de premios indexada $O(1)$ vía mapa hash en vez de búsqueda lineal $O(N \times M)$.
 * - Proyección directa de bandas relacionadas vía [mapNotNull] evitando asignaciones intermedias.
 *
 * @property serviceAdapter Adaptador de red hacia la API de Vinilos.
 * @property defaultDispatcher Despachador para transformaciones y cálculos (por defecto [Dispatchers.Default]).
 */
class ArtistRepository @JvmOverloads constructor(
    private val serviceAdapter: ArtistServiceAdapter = RetrofitArtistServiceAdapter(),
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    /**
     * Obtiene el listado consolidado de todos los artistas (músicos y bandas)
     * para el catálogo de la HU03. Ejecuta ambas peticiones en paralelo mediante [async].
     * La consolidación y mapeo a [Performer] se realiza fuera del hilo principal.
     */
    suspend fun getArtists(): List<Performer> = coroutineScope {
        val musiciansDeferred = async { serviceAdapter.getMusicians() }
        val bandsDeferred = async { serviceAdapter.getAllBands() }

        val musicians = musiciansDeferred.await()
        val bands = bandsDeferred.await()

        withContext(defaultDispatcher) {
            val musicianPerformers = musicians.map { m ->
                Performer(
                    id = m.id,
                    name = m.name,
                    image = m.image,
                    description = m.description,
                    birthDate = m.birthDate,
                    creationDate = "",
                    albums = m.albums,
                )
            }

            val bandPerformers = bands.map { b ->
                Performer(
                    id = b.id,
                    name = b.name,
                    image = b.image,
                    description = b.description,
                    birthDate = "",
                    creationDate = b.creationDate,
                    albums = b.albums,
                )
            }

            musicianPerformers + bandPerformers
        }
    }

    /**
     * Obtiene el detalle consolidado del artista [id]. Si [isBandHint] es nulo,
     * intenta resolverlo primero como músico y luego como banda.
     */
    suspend fun getArtistDetail(id: Int, isBandHint: Boolean? = null): ArtistDetail = when (isBandHint) {
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

    /**
     * Consulta el músico y recupera en paralelo (con [async]) las bandas relacionadas,
     * los premios y los coleccionistas para minimizar la latencia percibida por el usuario.
     * Las peticiones remotas se ejecutan en el adaptador y el ensamblado del detalle se
     * realiza en [defaultDispatcher].
     */
    private suspend fun fetchMusicianDetail(musicianId: Int): ArtistDetail = coroutineScope {
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

        val musician = musicianDeferred.await()
        val allBands = allBandsDeferred.await()
        val allPrizes = allPrizesDeferred.await()
        val allCollectors = allCollectorsDeferred.await()

        withContext(defaultDispatcher) {
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
    }

    /**
     * Consulta la banda y recupera en paralelo (con [async]) premios y coleccionistas.
     * Las peticiones remotas se ejecutan en el adaptador y el ensamblado del detalle se
     * realiza en [defaultDispatcher].
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

        withContext(defaultDispatcher) {
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
}
