package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.ArtistServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.BandDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.MusicianDetailResponse
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.Prize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistRepositoryTest {

    private open class FakeArtistServiceAdapter(
        private val musicians: List<MusicianDetailResponse> = emptyList(),
        private val bands: List<BandDetailResponse> = emptyList(),
        private val networkError: Exception? = null,
    ) : ArtistServiceAdapter {
        override suspend fun getMusicians(): List<MusicianDetailResponse> {
            networkError?.let { throw it }
            return musicians
        }

        override suspend fun getMusician(id: Int): MusicianDetailResponse {
            networkError?.let { throw it }
            return musicians.first { it.id == id }
        }

        override suspend fun getAllBands(): List<BandDetailResponse> {
            networkError?.let { throw it }
            return bands
        }

        override suspend fun getBand(id: Int): BandDetailResponse {
            networkError?.let { throw it }
            return bands.first { it.id == id }
        }

        override suspend fun getPrizes(): List<Prize> = emptyList()
        override suspend fun getCollectors(): List<Collector> = emptyList()
    }

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun getArtists_combinaMusicosYBandasCorrectamente() = runTest(testDispatcher) {
        val musician = MusicianDetailResponse(id = 1, name = "Joan Manuel Serrat", birthDate = "1943-12-27")
        val band = BandDetailResponse(id = 2, name = "Queen", creationDate = "1970-01-01")
        val repository = ArtistRepository(
            serviceAdapter = FakeArtistServiceAdapter(musicians = listOf(musician), bands = listOf(band)),
            defaultDispatcher = testDispatcher,
        )

        val result = repository.getArtists()

        assertEquals(2, result.size)
        val serrat = result.first { it.id == 1 }
        assertFalse(serrat.isBand)
        assertEquals("Joan Manuel Serrat", serrat.name)

        val queen = result.first { it.id == 2 }
        assertTrue(queen.isBand)
        assertEquals("Queen", queen.name)
    }

    @Test(expected = IOException::class)
    fun getArtists_anteErrorDeRed_lanzaExcepcion() = runTest(testDispatcher) {
        val repository = ArtistRepository(
            serviceAdapter = FakeArtistServiceAdapter(networkError = IOException("Error de conexión")),
            defaultDispatcher = testDispatcher,
        )

        repository.getArtists()
    }
}
