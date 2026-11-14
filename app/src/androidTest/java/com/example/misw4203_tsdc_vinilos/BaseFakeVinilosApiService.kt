package com.example.misw4203_tsdc_vinilos

import com.example.misw4203_tsdc_vinilos.data.network.BandDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.MusicianDetailResponse
import com.example.misw4203_tsdc_vinilos.data.network.TrackRequest
import com.example.misw4203_tsdc_vinilos.data.network.VinilosApiService
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorAlbum
import com.example.misw4203_tsdc_vinilos.model.Prize
import com.example.misw4203_tsdc_vinilos.model.Track

/**
 * Base de los servicios HTTP falsos de las pruebas E2E. Responde listas vacías y "no encontrado"
 * en todos los endpoints; cada prueba sobrescribe solo los que usa, de modo que agregar un
 * endpoint nuevo a [VinilosApiService] no obliga a modificar todas las pruebas.
 */
internal open class BaseFakeVinilosApiService : VinilosApiService {

    override suspend fun getAlbums(): List<Album> = emptyList()

    override suspend fun getAlbum(albumId: Int): Album =
        throw NoSuchElementException("Album $albumId not found")

    override suspend fun createAlbum(album: Album): Album = album

    override suspend fun postTrack(albumId: Int, track: TrackRequest): Track =
        Track(id = 999, name = track.name, duration = track.duration)

    override suspend fun getMusicians(): List<MusicianDetailResponse> = emptyList()

    override suspend fun getMusician(musicianId: Int): MusicianDetailResponse =
        throw NoSuchElementException("Musician $musicianId not found")

    override suspend fun getBands(): List<BandDetailResponse> = emptyList()

    override suspend fun getBand(bandId: Int): BandDetailResponse =
        throw NoSuchElementException("Band $bandId not found")

    override suspend fun getPrizes(): List<Prize> = emptyList()

    override suspend fun getCollectors(): List<Collector> = emptyList()

    override suspend fun getCollector(collectorId: Int): Collector =
        throw NoSuchElementException("Collector $collectorId not found")

    override suspend fun getCollectorAlbums(collectorId: Int): List<CollectorAlbum> = emptyList()
}
