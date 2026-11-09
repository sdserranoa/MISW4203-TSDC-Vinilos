package com.example.misw4203_tsdc_vinilos.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas unitarias del resumen de coleccionista que usa el listado (HU05). */
class CollectorSummaryTest {

    @Test
    fun toSummary_cuentaLosAlbumesDeLaColeccion() {
        val collector = Collector(
            id = 100,
            name = "Manolo Bellon",
            email = "manollo@caracol.com.co",
            collectorAlbums = listOf(CollectorAlbum(1), CollectorAlbum(2)),
        )

        assertEquals(CollectorSummary(100, "Manolo Bellon", "manollo@caracol.com.co", 2), collector.toSummary())
    }

    @Test
    fun initialsOf_tomaLasDosPrimerasPalabrasEnMayuscula() {
        assertEquals("MB", initialsOf("Manolo Bellon"))
        assertEquals("RB", initialsOf("rubén blades bellido"))
    }

    @Test
    fun initialsOf_conUnaPalabraOEspaciosExtra_seCalculaCorrectamente() {
        assertEquals("A", initialsOf("Ana"))
        assertEquals("JM", initialsOf("  Jaime   Monsalve "))
        assertEquals("", initialsOf("   "))
    }
}
