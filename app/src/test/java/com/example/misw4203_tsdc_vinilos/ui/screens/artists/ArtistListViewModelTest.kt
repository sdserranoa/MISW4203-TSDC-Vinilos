package com.example.misw4203_tsdc_vinilos.ui.screens.artists

import com.example.misw4203_tsdc_vinilos.data.network.BandDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.MusicianDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.RetrofitArtistServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.repository.ArtistRepository
import com.example.misw4203_tsdc_vinilos.testutil.FakeVinilosApiService
import com.example.misw4203_tsdc_vinilos.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class ArtistListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private fun viewModelWith(service: FakeVinilosApiService) = ArtistListViewModel(
        repository = ArtistRepository(
            serviceAdapter = RetrofitArtistServiceAdapter(api = service),
            ioDispatcher = testDispatcher,
        ),
        defaultDispatcher = testDispatcher,
    )

    private fun ArtistListViewModel.successState() = uiState.value as ArtistListUiState.Success

    private val sampleMusicians = (1..12).map {
        MusicianDetailResponse(id = it, name = "Solista $it", birthDate = "1980-01-01")
    }
    private val sampleBands = (13..18).map {
        BandDetailResponse(id = it, name = "Banda $it", creationDate = "1990-01-01")
    }

    @Test
    fun cargaInicial_conArtistas_muestraPrimeraPagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(musicians = sampleMusicians, bands = sampleBands))

        val state = viewModel.successState()
        assertEquals(1, state.currentPage)
        assertEquals(2, state.totalPages)
        assertEquals(18, state.totalArtists)
        assertEquals(10, state.artists.size)
    }

    @Test
    fun filtro_porTexto_filtraArtistasPorNombre() {
        val viewModel = viewModelWith(FakeVinilosApiService(musicians = sampleMusicians, bands = sampleBands))

        viewModel.onSearchQueryChange("Banda")

        val state = viewModel.successState()
        assertEquals(6, state.totalArtists)
        assertTrue(state.artists.all { it.isBand })
    }

    @Test
    fun filtro_porTipoBandas_muestraSoloBandas() {
        val viewModel = viewModelWith(FakeVinilosApiService(musicians = sampleMusicians, bands = sampleBands))

        viewModel.onFilterTypeChange(ArtistFilterType.BANDS)

        val state = viewModel.successState()
        assertEquals(6, state.totalArtists)
        assertTrue(state.artists.all { it.isBand })
    }

    @Test
    fun filtro_porTipoSolistas_muestraSoloMusicos() {
        val viewModel = viewModelWith(FakeVinilosApiService(musicians = sampleMusicians, bands = sampleBands))

        viewModel.onFilterTypeChange(ArtistFilterType.MUSICIANS)

        val state = viewModel.successState()
        assertEquals(12, state.totalArtists)
        assertTrue(state.artists.all { !it.isBand })
    }

    @Test
    fun paginacion_avanzarYRetroceder_cambiaDePagina() {
        val viewModel = viewModelWith(FakeVinilosApiService(musicians = sampleMusicians, bands = sampleBands))

        viewModel.nextPage()
        var state = viewModel.successState()
        assertEquals(2, state.currentPage)

        viewModel.previousPage()
        state = viewModel.successState()
        assertEquals(1, state.currentPage)
    }

    @Test
    fun cargaInicial_sinArtistas_muestraEstadoEmpty() {
        val viewModel = viewModelWith(FakeVinilosApiService())

        assertEquals(ArtistListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun cargaInicial_errorDeRed_muestraEstadoError() {
        val viewModel = viewModelWith(FakeVinilosApiService(error = IOException("Red no disponible")))

        assertEquals(ArtistListUiState.Error, viewModel.uiState.value)
    }
}
