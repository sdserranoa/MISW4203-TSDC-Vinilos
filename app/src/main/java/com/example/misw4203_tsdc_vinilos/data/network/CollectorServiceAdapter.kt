package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Collector
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service Adapter del dominio de coleccionistas (HU05 y HU06).
 *
 * Es el único punto de la app que conoce los recursos `/collectors` de BackVynils: el repositorio
 * depende de esta interfaz y no del cliente HTTP, por lo que la fuente remota se puede reemplazar
 * (por ejemplo, por un adaptador falso en las pruebas) sin tocar las capas superiores.
 */
interface CollectorServiceAdapter {

    /** Todos los coleccionistas registrados (`GET /collectors`). */
    suspend fun getCollectors(): List<Collector>

    /** Coleccionista identificado por [collectorId] (`GET /collectors/{id}`). */
    suspend fun getCollector(collectorId: Int): Collector
}

/**
 * Implementación del [CollectorServiceAdapter] sobre Retrofit. Las peticiones y la conversión del
 * JSON se ejecutan en [ioDispatcher], así que las llamadas son seguras desde el hilo principal y
 * no bloquean la interfaz (prevención de ANR).
 */
class RetrofitCollectorServiceAdapter(
    private val api: VinilosApiService = VinilosApi.service,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CollectorServiceAdapter {

    override suspend fun getCollectors(): List<Collector> =
        withContext(ioDispatcher) { api.getCollectors() }

    override suspend fun getCollector(collectorId: Int): Collector =
        withContext(ioDispatcher) { api.getCollector(collectorId) }
}
