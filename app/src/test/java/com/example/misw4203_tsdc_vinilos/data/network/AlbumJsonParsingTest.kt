package com.example.misw4203_tsdc_vinilos.data.network

import com.example.misw4203_tsdc_vinilos.model.Album
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifica que el modelo [Album] interpreta la respuesta real de `GET /albums` de BackVynils
 * y genera el cuerpo que acepta `POST /albums` (HU07).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
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

    @Test
    fun albumNuevo_seSerializaSoloConLosCamposQueAceptaPostAlbums() {
        val newAlbum = Album(
            name = "Álbum 2024",
            cover = "https://example.com/cover.jpg",
            releaseDate = "2024-02-29",
            description = "Música clásica, edición 2024.",
            genre = "Classical",
            recordLabel = "Sony Music",
        )

        val body = VinilosApi.json.parseToJsonElement(VinilosApi.json.encodeToString(newAlbum)).jsonObject

        // BackVynils rechaza claves desconocidas (p. ej. `id` o colecciones anidadas).
        assertEquals(
            setOf("name", "cover", "releaseDate", "description", "genre", "recordLabel"),
            body.keys,
        )
        assertEquals("Álbum 2024", body.getValue("name").jsonPrimitive.content)
        assertEquals("2024-02-29", body.getValue("releaseDate").jsonPrimitive.content)
        assertEquals("Classical", body.getValue("genre").jsonPrimitive.content)
        assertEquals("Sony Music", body.getValue("recordLabel").jsonPrimitive.content)
    }
}
