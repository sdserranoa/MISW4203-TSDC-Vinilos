package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.CollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.RetrofitCollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorDetail
import com.example.misw4203_tsdc_vinilos.model.CollectorSummary
import com.example.misw4203_tsdc_vinilos.model.toDetail
import com.example.misw4203_tsdc_vinilos.model.toSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Fuente única de datos de coleccionistas para la capa de presentación. Obtiene la información a
 * través del [CollectorServiceAdapter], sin depender de Retrofit ni de la URL del backend.
 */
class CollectorRepository(
    private val serviceAdapter: CollectorServiceAdapter = RetrofitCollectorServiceAdapter(),
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    /**
     * Listado de coleccionistas para HU05. La conversión a [CollectorSummary] se hace fuera del
     * hilo principal y descarta los datos anidados que la tarjeta del listado no muestra.
     */
    suspend fun getCollectorSummaries(): List<CollectorSummary> {
        val collectors = serviceAdapter.getCollectors()
        return withContext(defaultDispatcher) { collectors.map(Collector::toSummary) }
    }

    /**
     * Detalle del coleccionista [collectorId] para HU06. Los datos del coleccionista y sus álbumes
     * vienen de dos recursos independientes, así que se consultan en paralelo: la espera total es
     * la de la petición más lenta y no la suma de ambas. Si una falla, la otra se cancela. La unión
     * de ambas respuestas se hace fuera del hilo principal.
     */
    suspend fun getCollectorDetail(collectorId: Int): CollectorDetail = coroutineScope {
        val collector = async { serviceAdapter.getCollector(collectorId) }
        val albums = async { serviceAdapter.getCollectorAlbums(collectorId) }
        val collectorResponse = collector.await()
        val albumsResponse = albums.await()
        withContext(defaultDispatcher) { collectorResponse.toDetail(albumsResponse) }
    }
}
