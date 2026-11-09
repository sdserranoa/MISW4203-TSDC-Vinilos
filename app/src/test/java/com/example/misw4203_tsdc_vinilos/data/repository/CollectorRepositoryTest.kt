package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.CollectorServiceAdapter
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorAlbum
import com.example.misw4203_tsdc_vinilos.model.CollectorSummary
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.testutil.testAlbum
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import com.example.misw4203_tsdc_vinilos.testutil.testCollectorAlbum
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

/**
 * Pruebas unitarias del repositorio de coleccionistas. El repositorio depende solo de la interfaz
 * [CollectorServiceAdapter], así que aquí se reemplaza por un adaptador falso sin red ni Retrofit.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectorRepositoryTest {

    private open class FakeCollectorServiceAdapter(
        private val collectors: List<Collector> = emptyList(),
        private val albumsByCollector: Map<Int, List<CollectorAlbum>> = emptyMap(),
        private val collectorsError: Exception? = null,
        private val albumsError: Exception? = null,
    ) : CollectorServiceAdapter {
        override suspend fun getCollectors(): List<Collector> {
            collectorsError?.let { throw it }
            return collectors
        }

        override suspend fun getCollector(collectorId: Int): Collector {
            collectorsError?.let { throw it }
            return collectors.first { it.id == collectorId }
        }

        override suspend fun getCollectorAlbums(collectorId: Int): List<CollectorAlbum> {
            albumsError?.let { throw it }
            return albumsByCollector[collectorId].orEmpty()
        }
    }

    private val manolo = testCollector(
        id = 100,
        name = "Manolo Bellon",
        email = "manollo@caracol.com.co",
        telephone = "3502457896",
        favoritePerformers = listOf(Performer(100, "Rubén Blades Bellido de Luna")),
        comments = listOf(Comment(100, "The most relevant album of Ruben Blades", 5)),
    )
    private val buscandoAmerica = testAlbum(100, name = "Buscando América", genre = "Salsa")

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
    fun getCollectorSummaries_conErrorDelAdaptador_propagaLaExcepcion() = runTest {
        val repository = CollectorRepository(
            serviceAdapter = FakeCollectorServiceAdapter(collectorsError = IOException("Sin conexión")),
            defaultDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val result = runCatching { repository.getCollectorSummaries() }

        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun getCollectorDetail_uneDatosDeContactoAlbumesFavoritosYComentarios() = runTest {
        val repository = CollectorRepository(
            serviceAdapter = FakeCollectorServiceAdapter(
                collectors = listOf(manolo),
                albumsByCollector = mapOf(100 to listOf(testCollectorAlbum(buscandoAmerica))),
            ),
        )

        val detail = repository.getCollectorDetail(100)

        assertEquals("Manolo Bellon", detail.name)
        assertEquals("3502457896", detail.telephone)
        assertEquals("manollo@caracol.com.co", detail.email)
        assertEquals(listOf("Buscando América"), detail.albums.map { it.name })
        assertEquals(listOf("Rubén Blades Bellido de Luna"), detail.favoritePerformers.map { it.name })
        assertEquals(listOf(5), detail.comments.map { it.rating })
    }

    @Test
    fun getCollectorDetail_consultaColeccionistaYAlbumesEnParalelo() = runTest(timeout = 5.seconds) {
        // El coleccionista solo responde cuando ya se pidieron sus álbumes: si el repositorio
        // hiciera las consultas una tras otra, esta prueba quedaría esperando y fallaría.
        val albumsRequested = CompletableDeferred<Unit>()
        val adapter = object : FakeCollectorServiceAdapter(
            collectors = listOf(manolo),
            albumsByCollector = mapOf(100 to listOf(testCollectorAlbum(buscandoAmerica))),
        ) {
            override suspend fun getCollector(collectorId: Int): Collector {
                albumsRequested.await()
                return super.getCollector(collectorId)
            }

            override suspend fun getCollectorAlbums(collectorId: Int): List<CollectorAlbum> {
                albumsRequested.complete(Unit)
                return super.getCollectorAlbums(collectorId)
            }
        }

        val detail = CollectorRepository(serviceAdapter = adapter).getCollectorDetail(100)

        assertEquals(1, detail.albums.size)
    }

    @Test
    fun getCollectorDetail_siFallanLosAlbumes_propagaLaExcepcion() = runTest {
        val repository = CollectorRepository(
            serviceAdapter = FakeCollectorServiceAdapter(
                collectors = listOf(manolo),
                albumsError = IOException("Sin conexión"),
            ),
        )

        val result = runCatching { repository.getCollectorDetail(100) }

        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun getCollectorDetail_siFallaElColeccionista_propagaLaExcepcion() = runTest {
        val repository = CollectorRepository(
            serviceAdapter = FakeCollectorServiceAdapter(collectorsError = IOException("Sin conexión")),
        )

        val result = runCatching { repository.getCollectorDetail(100) }

        assertTrue(result.exceptionOrNull() is IOException)
    }
}
