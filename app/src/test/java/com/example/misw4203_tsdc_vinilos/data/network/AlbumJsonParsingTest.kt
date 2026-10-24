package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Album
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifica que el modelo [Album] interpreta la respuesta real de `GET /albums` de BackVynils. */
class AlbumJsonParsingTest {

    private val response = """
        [
          {
            "id": 100,
            "name": "Buscando América",
            "cover": "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
            "releaseDate": "1984-08-01T00:00:00.000Z",
            "description": "Buscando América es el primer álbum de la banda de Rubén Blades.",
            "genre": "Salsa",
            "recordLabel": "Elektra",
            "tracks": [{ "id": 100, "name": "Decisiones", "duration": "5:05" }],
            "performers": [
              {
                "id": 100,
                "name": "Rubén Blades Bellido de Luna",
                "image": "https://upload.wikimedia.org/wikipedia/commons/b/bb/Ruben_Blades_by_Gage_Skidmore.jpg",
                "description": "Es un cantante, compositor, músico, actor, abogado, político y activista panameño.",
                "birthDate": "1948-07-16T00:00:00.000Z"
              }
            ],
            "comments": []
          }
        ]
    """.trimIndent()

    @Test
    fun respuestaDeAlbumes_seConvierteEnModeloIgnorandoCamposNoUsados() {
        val albums = VinilosApi.json.decodeFromString<List<Album>>(response)

        val album = albums.single()
        assertEquals(100, album.id)
        assertEquals("Buscando América", album.name)
        assertEquals("Salsa", album.genre)
        assertEquals("1984", album.releaseYear)
        assertEquals("Rubén Blades Bellido de Luna", album.performers.single().name)
    }
}
