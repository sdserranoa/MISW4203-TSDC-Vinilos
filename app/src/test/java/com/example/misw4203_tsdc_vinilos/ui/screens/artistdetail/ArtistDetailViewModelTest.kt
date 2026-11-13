package com.example.misw4203_tsdc_vinilos.ui.screens.artistdetail

import com.example.misw4203_tsdc_vinilos.data.network.ArtistServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.BandDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.MusicianDetailResponse
import com.example.misw4203_tsdc_vinilos.data.repository.ArtistRepository
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.Prize
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeArtistServiceAdapter(
        var musicianToReturn: MusicianDetailResponse? = null,
        var bandToReturn: BandDetailResponse? = null,
        var allBandsToReturn: List<BandDetailResponse> = emptyList(),
        var error: Throwable? = null,
    ) : ArtistServiceAdapter {
        override suspend fun getMusician(id: Int): MusicianDetailResponse {
            error?.let { throw it }
            return musicianToReturn ?: throw NoSuchElementException()
        }

        override suspend fun getMusicians(): List<MusicianDetailResponse> = emptyList()

        override suspend fun getBand(id: Int): BandDetailResponse {
            error?.let { throw it }
            return bandToReturn ?: throw NoSuchElementException()
        }

        override suspend fun getAllBands(): List<BandDetailResponse> = allBandsToReturn

        override suspend fun getPrizes(): List<Prize> = emptyList()

        override suspend fun getCollectors(): List<Collector> = emptyList()
    }

    @Test
    fun alInicializar_cargaMusicoExitosamente() = runTest {
        val adapter = FakeArtistServiceAdapter(
            musicianToReturn = MusicianDetailResponse(
                id = 1,
                name = "Gustavo Cerati",
                birthDate = "1959-08-11",
                description = "Músico argentino"
            )
        )
        val repository = ArtistRepository(serviceAdapter = adapter, defaultDispatcher = UnconfinedTestDispatcher(testScheduler))
        val viewModel = ArtistDetailViewModel(artistId = 1, isBand = false, repository = repository)

        val state = viewModel.uiState.value
        assertTrue(state is ArtistDetailUiState.Success)
        val success = state as ArtistDetailUiState.Success
        assertEquals("Gustavo Cerati", success.artist.name)
        assertFalse(success.isFavorite)
    }

    @Test
    fun alInicializar_cargaBandaExitosamente() = runTest {
        val adapter = FakeArtistServiceAdapter(
            bandToReturn = BandDetailResponse(
                id = 50,
                name = "Soda Stereo",
                creationDate = "1982-01-01",
                description = "Banda argentina"
            )
        )
        val repository = ArtistRepository(serviceAdapter = adapter, defaultDispatcher = UnconfinedTestDispatcher(testScheduler))
        val viewModel = ArtistDetailViewModel(artistId = 50, isBand = true, repository = repository)

        val state = viewModel.uiState.value
        assertTrue(state is ArtistDetailUiState.Success)
        val success = state as ArtistDetailUiState.Success
        assertEquals("Soda Stereo", success.artist.name)
        assertTrue(success.artist.isBand)
    }

    @Test
    fun alFallarCarga_estadoPasaAError() = runTest {
        val adapter = FakeArtistServiceAdapter(error = IOException("Red no disponible"))
        val repository = ArtistRepository(serviceAdapter = adapter, defaultDispatcher = UnconfinedTestDispatcher(testScheduler))
        val viewModel = ArtistDetailViewModel(artistId = 1, repository = repository)

        val state = viewModel.uiState.value
        assertTrue(state is ArtistDetailUiState.Error)
    }

    @Test
    fun toggleFavorite_alternaEstadoExitosamente() = runTest {
        val adapter = FakeArtistServiceAdapter(
            musicianToReturn = MusicianDetailResponse(id = 1, name = "Gustavo Cerati")
        )
        val repository = ArtistRepository(serviceAdapter = adapter, defaultDispatcher = UnconfinedTestDispatcher(testScheduler))
        val viewModel = ArtistDetailViewModel(artistId = 1, repository = repository)

        assertFalse((viewModel.uiState.value as ArtistDetailUiState.Success).isFavorite)
        viewModel.toggleFavorite()
        assertTrue((viewModel.uiState.value as ArtistDetailUiState.Success).isFavorite)
        viewModel.toggleFavorite()
        assertFalse((viewModel.uiState.value as ArtistDetailUiState.Success).isFavorite)
    }
}
