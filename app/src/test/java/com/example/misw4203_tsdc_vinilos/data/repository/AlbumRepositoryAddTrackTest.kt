package com.example.misw4203_tsdc_vinilos.data.repository

import com.example.misw4203_tsdc_vinilos.data.network.AlbumServiceAdapter
import com.example.misw4203_tsdc_vinilos.data.network.TrackRequest
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumRepositoryAddTrackTest {

    private class FakeAlbumServiceAdapter(
        private val postError: Exception? = null,
    ) : AlbumServiceAdapter {
        var lastPostedAlbumId: Int? = null
        var lastPostedTrack: TrackRequest? = null

        override suspend fun getAlbums(): List<Album> = emptyList()

        override suspend fun getAlbum(id: Int): Album =
            Album(id = id, name = "Abbey Road", cover = "", releaseDate = "1969", description = "", genre = "Rock", recordLabel = "EMI")

        override suspend fun postTrack(albumId: Int, track: TrackRequest): Track {
            postError?.let { throw it }
            lastPostedAlbumId = albumId
            lastPostedTrack = track
            return Track(id = 100, name = track.name, duration = track.duration)
        }
    }

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun addTrackToAlbum_enviaInformacionCorrectaYRetornaTrack() = runTest(testDispatcher) {
        val fakeAdapter = FakeAlbumServiceAdapter()
        val repository = AlbumRepository(
            serviceAdapter = fakeAdapter,
        )

        val result = repository.addTrackToAlbum(albumId = 11, name = "Decisiones", duration = "5:05")

        assertEquals(11, fakeAdapter.lastPostedAlbumId)
        assertEquals("Decisiones", fakeAdapter.lastPostedTrack?.name)
        assertEquals("5:05", fakeAdapter.lastPostedTrack?.duration)
        assertEquals("Decisiones", result.name)
        assertEquals("5:05", result.duration)
    }

    @Test(expected = IOException::class)
    fun addTrackToAlbum_anteErrorDeRed_lanzaExcepcion() = runTest(testDispatcher) {
        val repository = AlbumRepository(
            serviceAdapter = FakeAlbumServiceAdapter(postError = IOException("Sin conexión")),
        )

        repository.addTrackToAlbum(albumId = 11, name = "Decisiones", duration = "5:05")
    }
}
