package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.CollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.RetrofitCollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorSummary
import com.example.misw4203_tsdc_vinilos.model.toSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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

    /** Información completa del coleccionista [collectorId]. */
    suspend fun getCollector(collectorId: Int): Collector = serviceAdapter.getCollector(collectorId)
}
