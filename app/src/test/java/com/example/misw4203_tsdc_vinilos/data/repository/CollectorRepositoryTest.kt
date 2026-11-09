package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.CollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorSummary
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Pruebas unitarias del repositorio de coleccionistas. El repositorio depende solo de la interfaz
 * [CollectorServiceAdapter], así que aquí se reemplaza por un adaptador falso sin red ni Retrofit.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectorRepositoryTest {

    private class FakeCollectorServiceAdapter(
        private val collectors: List<Collector> = emptyList(),
        private val error: Exception? = null,
    ) : CollectorServiceAdapter {
        override suspend fun getCollectors(): List<Collector> {
            error?.let { throw it }
            return collectors
        }

        override suspend fun getCollector(collectorId: Int): Collector {
            error?.let { throw it }
            return collectors.first { it.id == collectorId }
        }
    }

    @Test
    fun getCollectorSummaries_conservaNombreCorreoYCantidadDeAlbumes() = runTest {
        val repository = CollectorRepository(
            serviceAdapter = FakeCollectorServiceAdapter(
                listOf(
                    testCollector(100, "Manolo Bellon", email = "manollo@caracol.com.co", albumCount = 3),
                    testCollector(101, "Jaime Monsalve", email = "jmonsalve@rtvc.com.co", albumCount = 0),
                )
            ),
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val summaries = repository.getCollectorSummaries()

        assertEquals(
            listOf(
                CollectorSummary(100, "Manolo Bellon", "manollo@caracol.com.co", 3),
                CollectorSummary(101, "Jaime Monsalve", "jmonsalve@rtvc.com.co", 0),
            ),
            summaries,
        )
        assertEquals("MB", summaries.first().initials)
    }

    @Test
    fun getCollector_devuelveLaInformacionCompletaDelColeccionista() = runTest {
        val repository = CollectorRepository(
            serviceAdapter = FakeCollectorServiceAdapter(listOf(testCollector(100, albumCount = 2))),
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val collector = repository.getCollector(100)

        assertEquals(100, collector.id)
        assertEquals(2, collector.collectorAlbums.size)
    }

    @Test
    fun getCollectorSummaries_conErrorDelAdaptador_propagaLaExcepcion() = runTest {
        val repository = CollectorRepository(
            serviceAdapter = FakeCollectorServiceAdapter(error = IOException("Sin conexión")),
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val result = runCatching { repository.getCollectorSummaries() }

        assertTrue(result.exceptionOrNull() is IOException)
    }
}
