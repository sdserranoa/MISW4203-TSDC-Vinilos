package com.example.misw4203_tsdc_vinilos.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas unitarias de la construcción del detalle de coleccionista (HU06). */
class CollectorDetailTest {

    private val buscandoAmerica = Album(100, "Buscando América", "", "1984-08-01T00:00:00.000Z", genre = "Salsa")
    private val poeta = Album(101, "Poeta del pueblo", "", "1984-08-01T00:00:00.000Z", genre = "Salsa")

    @Test
    fun toDetail_tomaLosAlbumesCompletosDeLaColeccion() {
        val collector = Collector(id = 100, name = "Manolo Bellon", telephone = "3502457896")

        val detail = collector.toDetail(
            listOf(
                CollectorAlbum(id = 1, price = 35.0, status = "Active", album = buscandoAmerica),
                CollectorAlbum(id = 2, price = 25.0, status = "Inactive", album = poeta),
            )
        )

        assertEquals(listOf(100, 101), detail.albums.map { it.id })
        assertEquals("3502457896", detail.telephone)
        assertEquals("MB", detail.initials)
    }

    @Test
    fun toDetail_descartaRelacionesSinAlbumYRegistrosRepetidos() {
        val collector = Collector(
            id = 100,
            name = "Manolo Bellon",
            favoritePerformers = listOf(Performer(1, "Queen"), Performer(1, "Queen")),
        )

        val detail = collector.toDetail(
            listOf(
                CollectorAlbum(id = 1, album = buscandoAmerica),
                CollectorAlbum(id = 2, album = buscandoAmerica),
                CollectorAlbum(id = 3, album = null),
            )
        )

        assertEquals(listOf(100), detail.albums.map { it.id })
        assertEquals(1, detail.favoritePerformers.size)
    }

    @Test
    fun toDetail_sinAlbumesNiFavoritosNiComentarios_dejaListasVacias() {
        val detail = Collector(id = 7, name = "Ana").toDetail(emptyList())

        assertTrue(detail.albums.isEmpty())
        assertTrue(detail.favoritePerformers.isEmpty())
        assertTrue(detail.comments.isEmpty())
    }
}
