package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.testCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.ContinuationInterceptor

/** Pruebas unitarias del Service Adapter de coleccionistas sobre Retrofit (HU05). */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectorServiceAdapterTest {

    private val collectors = listOf(testCollector(100, "Manolo Bellon"), testCollector(101, "Jaime Monsalve"))

    @Test
    fun getCollectors_delegaEnLaApiYDevuelveTodosLosColeccionistas() = runTest {
        val adapter = RetrofitCollectorServiceAdapter(
            api = FakeVinilosApiService(collectors = collectors),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        assertEquals(listOf(100, 101), adapter.getCollectors().map { it.id })
    }

    @Test
    fun getCollector_devuelveElColeccionistaSolicitado() = runTest {
        val adapter = RetrofitCollectorServiceAdapter(
            api = FakeVinilosApiService(collectors = collectors),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        assertEquals("Jaime Monsalve", adapter.getCollector(101).name)
    }

    @Test
    fun getCollectors_ejecutaLaPeticionEnElDispatcherDeEntradaSalida() = runTest {
        val ioDispatcher = StandardTestDispatcher(testScheduler)
        var dispatcherUsado: ContinuationInterceptor? = null
        val api = object : VinilosApiService by FakeVinilosApiService(collectors = collectors) {
            override suspend fun getCollectors() = collectors.also {
                dispatcherUsado = kotlin.coroutines.coroutineContext[ContinuationInterceptor]
            }
        }

        RetrofitCollectorServiceAdapter(api, ioDispatcher).getCollectors()

        assertEquals(ioDispatcher, dispatcherUsado)
    }

    @Test
    fun getCollectors_conErrorDeRed_propagaLaExcepcion() = runTest {
        val adapter = RetrofitCollectorServiceAdapter(
            api = FakeVinilosApiService(error = IOException("Sin conexión")),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val result = runCatching { adapter.getCollectors() }

        assertTrue(result.exceptionOrNull() is IOException)
    }
}
