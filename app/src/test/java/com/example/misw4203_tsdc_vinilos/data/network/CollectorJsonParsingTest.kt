package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Collector
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifica que el modelo [Collector] interpreta las respuestas reales de `GET /collectors` y
 * `GET /collectors/{id}` de BackVynils (contrato JSON de HU05).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class CollectorJsonParsingTest {

    private val collectorsResponse = """
        [
          {
            "id": 100,
            "name": "Manolo Bellon",
            "telephone": "3502457896",
            "email": "manollo@caracol.com.co",
            "comments": [
              { "id": 100, "description": "The most relevant album of Ruben Blades", "rating": 5 }
            ],
            "favoritePerformers": [
              {
                "id": 100,
                "name": "Rubén Blades Bellido de Luna",
                "image": "https://upload.wikimedia.org/wikipedia/commons/b/bb/Ruben_Blades_by_Gage_Skidmore.jpg",
                "description": "Es un cantante, compositor, músico, actor, abogado, político y activista panameño.",
                "birthDate": "1948-07-16T05:00:00.000Z"
              }
            ],
            "collectorAlbums": [
              { "id": 100, "price": 35, "status": "Active" }
            ]
          },
          {
            "id": 101,
            "name": "Jaime Monsalve",
            "telephone": "3012357936",
            "email": "jmonsalve@rtvc.com.co",
            "comments": [],
            "favoritePerformers": [],
            "collectorAlbums": []
          }
        ]
    """.trimIndent()

    @Test
    fun respuestaDeColeccionistas_seConvierteEnModeloConSusRelaciones() {
        val collectors = VinilosApi.json.decodeFromString<List<Collector>>(collectorsResponse)

        assertEquals(2, collectors.size)
        val manolo = collectors.first()
        assertEquals(100, manolo.id)
        assertEquals("Manolo Bellon", manolo.name)
        assertEquals("3502457896", manolo.telephone)
        assertEquals("manollo@caracol.com.co", manolo.email)
        assertEquals(1, manolo.collectorAlbums.size)
        assertEquals(35.0, manolo.collectorAlbums.single().price, 0.0)
        assertEquals("Active", manolo.collectorAlbums.single().status)
        assertNull(manolo.collectorAlbums.single().album)
        assertEquals("Rubén Blades Bellido de Luna", manolo.favoritePerformers.single().name)
        assertEquals(5, manolo.comments.single().rating)
    }

    @Test
    fun coleccionistaSinRelaciones_seConvierteConListasVacias() {
        val collectors = VinilosApi.json.decodeFromString<List<Collector>>(collectorsResponse)

        val jaime = collectors.last()
        assertTrue(jaime.collectorAlbums.isEmpty())
        assertTrue(jaime.favoritePerformers.isEmpty())
        assertTrue(jaime.comments.isEmpty())
    }

    @Test
    fun respuestaMinima_usaValoresPorDefectoEnCamposOpcionales() {
        val collector = VinilosApi.json.decodeFromString<Collector>("""{ "id": 7, "name": "Ana" }""")

        assertEquals(7, collector.id)
        assertEquals("", collector.email)
        assertTrue(collector.collectorAlbums.isEmpty())
    }
}
