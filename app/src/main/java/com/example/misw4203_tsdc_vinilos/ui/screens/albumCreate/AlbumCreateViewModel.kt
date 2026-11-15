package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.misw4203_tsdc_vinilos.data.repository.AlbumRepository
import com.example.misw4203_tsdc_vinilos.model.Album
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.net.URI
import java.net.URISyntaxException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal const val MAX_ALBUM_NAME_LENGTH = 100
/** Valores que acepta BackVynils en `POST /albums`; cualquier otro responde 400. */
internal val ALBUM_GENRES = listOf("Classical", "Salsa", "Rock", "Folk")
internal val ALBUM_RECORD_LABELS = listOf("Sony Music", "EMI", "Discos Fuentes", "Elektra", "Fania Records")

internal const val MAX_ALBUM_DESCRIPTION_LENGTH = 500

private val allowedText = Regex("""[\p{L}\p{M}\p{N}\p{P}\p{Z}\s]+""")
private val letterOrNumber = Regex("""[\p{L}\p{N}]""")
private val dateFormat = Regex("""[0-9]{4}-[0-9]{2}-[0-9]{2}""")

private fun optionError(value: String, options: List<String>): AlbumCreateValidationError? = when {
    value.isBlank() -> AlbumCreateValidationError.REQUIRED
    value !in options -> AlbumCreateValidationError.INVALID_OPTION
    else -> null
}

private fun textError(value: String): AlbumCreateValidationError? = when {
    value.isBlank() -> AlbumCreateValidationError.REQUIRED
    !allowedText.matches(value) || !letterOrNumber.containsMatchIn(value) ->
        AlbumCreateValidationError.INVALID_TEXT
    else -> null
}

private fun isValidCoverUrl(value: String): Boolean {
    val uri = try {
        URI(value)
    } catch (_: URISyntaxException) {
        return false
    }
    return (uri.scheme.equals("http", ignoreCase = true) ||
        uri.scheme.equals("https", ignoreCase = true)) &&
        uri.rawAuthority != null &&
        value.toHttpUrlOrNull() != null
}

internal fun validateAlbum(album: Album): Map<AlbumCreateField, AlbumCreateValidationError> = buildMap {
    val nameError = when {
        album.name.codePointCount(0, album.name.length) > MAX_ALBUM_NAME_LENGTH ->
            AlbumCreateValidationError.NAME_TOO_LONG
        else -> textError(album.name)
    }
    nameError?.let { put(AlbumCreateField.NAME, it) }

    val cover = album.cover.trim()
    when {
        cover.isEmpty() -> put(AlbumCreateField.COVER, AlbumCreateValidationError.REQUIRED)
        !isValidCoverUrl(cover) ->
            put(AlbumCreateField.COVER, AlbumCreateValidationError.INVALID_URL)
    }

    val releaseDate = album.releaseDate
    when {
        releaseDate.isBlank() ->
            put(AlbumCreateField.RELEASE_DATE, AlbumCreateValidationError.REQUIRED)
        else -> {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val position = ParsePosition(0)
            if (!dateFormat.matches(releaseDate) ||
                parser.parse(releaseDate, position) == null ||
                position.index != releaseDate.length
            ) {
                put(AlbumCreateField.RELEASE_DATE, AlbumCreateValidationError.INVALID_DATE)
            }
        }
    }

    optionError(album.genre, ALBUM_GENRES)?.let { put(AlbumCreateField.GENRE, it) }
    optionError(album.recordLabel, ALBUM_RECORD_LABELS)?.let { put(AlbumCreateField.RECORD_LABEL, it) }
    textError(album.description)?.let { put(AlbumCreateField.DESCRIPTION, it) }
}

/** Formulario y gestión de estado para la creación de un nuevo álbum (HU07). */
class AlbumCreateViewModel(
    private val repository: AlbumRepository,
) : ViewModel() {

    private val emptyAlbum = Album(
        id = 0,
        name = "",
        cover = "",
        releaseDate = "",
        genre = "",
    )

    private val _uiState = MutableStateFlow<AlbumCreateUiState>(
        AlbumCreateUiState.Success(album = emptyAlbum)
    )

    val uiState: StateFlow<AlbumCreateUiState> = _uiState.asStateFlow()

    var album: Album = emptyAlbum
        private set

    private var validationAttempted = false

    fun onNameChange(name: String) {
        if (name.codePointCount(0, name.length) <= MAX_ALBUM_NAME_LENGTH) {
            album = album.copy(name = name)
            updateSuccessState()
        }
    }

    fun onCoverChange(cover: String) {
        album = album.copy(cover = cover)
        updateSuccessState()
    }

    fun onReleaseDateChange(releaseDate: String) {
        album = album.copy(releaseDate = releaseDate)
        updateSuccessState()
    }

    fun onDescriptionChange(description: String) {
        if (description.length <= MAX_ALBUM_DESCRIPTION_LENGTH) {
            album = album.copy(description = description)
            updateSuccessState()
        }
    }

    fun onGenreChange(genre: String) {
        album = album.copy(genre = genre)
        updateSuccessState()
    }

    fun onRecordLabelChange(recordLabel: String) {
        album = album.copy(recordLabel = recordLabel)
        updateSuccessState()
    }

    fun dismissSnackbar() {
        val currentState = _uiState.value
        if (currentState is AlbumCreateUiState.Success) {
            _uiState.value = currentState.copy(showSnackbar = false)
        }
    }

    fun saveAlbum(
        onSaveFailed: () -> Unit = {},
        onSaved: (Album) -> Unit = {},
    ) {
        val currentState = _uiState.value as? AlbumCreateUiState.Success ?: return
        validationAttempted = true
        val albumToSave = album.copy(cover = album.cover.trim())
        val errors = validateAlbum(albumToSave)
        if (errors.isNotEmpty()) {
            _uiState.value = currentState.copy(validationErrors = errors)
            return
        }
        _uiState.value = currentState.copy(
            isSaving = true,
            errorMessage = null,
            validationErrors = emptyMap(),
        )

        viewModelScope.launch {
            val created = try {
                repository.createAlbum(albumToSave)
            } catch (e: CancellationException) {
                val state = _uiState.value
                if (state is AlbumCreateUiState.Success) {
                    _uiState.value = state.copy(isSaving = false)
                }
                throw e
            } catch (e: Exception) {
                _uiState.value = AlbumCreateUiState.SaveError
                onSaveFailed()
                return@launch
            }
            album = created
            _uiState.value = AlbumCreateUiState.Success(
                album = created,
                isSaving = false,
                isCreated = true,
                showSnackbar = true,
            )
            onSaved(created)
        }
    }

    fun newAlbum() {
        album = emptyAlbum
        validationAttempted = false
        _uiState.value = AlbumCreateUiState.Success(album = emptyAlbum)
    }

    private fun updateSuccessState() {
        val currentState = _uiState.value
        if (currentState is AlbumCreateUiState.Success) {
            _uiState.value = currentState.copy(
                album = album,
                validationErrors = if (validationAttempted) validateAlbum(album) else emptyMap(),
            )
        } else {
            _uiState.value = AlbumCreateUiState.Success(
                album = album,
                validationErrors = if (validationAttempted) validateAlbum(album) else emptyMap(),
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AlbumCreateViewModel(AlbumRepository()) }
        }
    }
}
