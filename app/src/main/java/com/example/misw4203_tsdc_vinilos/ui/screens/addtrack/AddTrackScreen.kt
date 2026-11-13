package com.example.misw4203_tsdc_vinilos.ui.screens.addtrack

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.ui.components.LoadingMessage
import com.example.misw4203_tsdc_vinilos.ui.components.StateMessage
import com.example.misw4203_tsdc_vinilos.ui.theme.Amber
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** HU08 - Asociar canciones a un álbum. */
@Composable
fun AddTrackScreen(
    albumId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onErrorBack: () -> Unit = {},
    viewModel: AddTrackViewModel = viewModel(
        factory = AddTrackViewModel.provideFactory(albumId = albumId)
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddTrackContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadAlbumHeader,
        onNameChange = viewModel::onNameChange,
        onDurationChange = viewModel::onDurationChange,
        onSuggestionSelected = viewModel::onSuggestionSelected,
        onSubmit = {
            viewModel.submitTrack(
                onSuccessNavBack = onSuccess,
                onErrorNavBack = onErrorBack,
            )
        },
        modifier = modifier,
    )
}

@Composable
fun AddTrackContent(
    uiState: AddTrackUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit = {},
    onDurationChange: (String) -> Unit = {},
    onSuggestionSelected: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AddTrackTopBar(onBack = onBack)
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            AddTrackUiState.Loading -> LoadingMessage(
                message = stringResource(R.string.add_track_loading),
                modifier = contentModifier,
            )

            is AddTrackUiState.ErrorLoadingAlbum -> StateMessage(
                message = stringResource(R.string.add_track_error),
                modifier = contentModifier,
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
            )

            is AddTrackUiState.Form -> AddTrackFormBody(
                formState = uiState,
                onCancel = onBack,
                onNameChange = onNameChange,
                onDurationChange = onDurationChange,
                onSuggestionSelected = onSuggestionSelected,
                onSubmit = onSubmit,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun AddTrackTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.artist_back),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = stringResource(R.string.add_track_screen_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun AddTrackFormBody(
    formState: AddTrackUiState.Form,
    onCancel: () -> Unit,
    onNameChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                // Título de la tarjeta del formulario + Botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_badge),
                            contentDescription = null,
                            tint = Amber,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = stringResource(R.string.add_track_dialog_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    IconButton(onClick = onCancel) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.add_track_close_description),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resumen del álbum
                AlbumHeaderSummary(album = formState.album)

                Spacer(modifier = Modifier.height(20.dp))

                // Campo 1: Nombre de la canción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.add_track_name_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${formState.name.length} / 60",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = formState.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(R.string.add_track_name_placeholder))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_vinyl),
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ⓘ ${stringResource(R.string.add_track_name_helper)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo 2: Duración
                Text(
                    text = stringResource(R.string.add_track_duration_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = formState.duration,
                    onValueChange = onDurationChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(R.string.add_track_duration_placeholder))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_clock),
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (formState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = formState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón principal de asociación (deshabilitado si los campos son inválidos)
                Button(
                    onClick = onSubmit,
                    enabled = formState.isValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber,
                        contentColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.add_track_submit_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de cancelación
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.add_track_cancel_button),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumHeaderSummary(
    album: Album,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (album.cover.isNotBlank()) {
                AsyncImage(
                    model = album.cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_vinyl),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val performerNames = album.performers.joinToString { it.name }
                val year = album.releaseDate.take(4)
                val subtitle = when {
                    performerNames.isNotBlank() && year.isNotBlank() -> "$performerNames • $year"
                    performerNames.isNotBlank() -> performerNames
                    else -> year
                }
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (album.genre.isNotBlank() || album.recordLabel.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Amber.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = album.genre.ifBlank { album.recordLabel },
                        style = MaterialTheme.typography.labelSmall,
                        color = Amber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AddTrackFormPreview() {
    val album = Album(
        id = 1,
        name = "Abbey Road",
        cover = "",
        releaseDate = "1969-09-26T00:00:00.000Z",
        genre = "Rock",
        recordLabel = "Apple",
    )
    MISW4203TSDCVinilosTheme {
        AddTrackFormBody(
            formState = AddTrackUiState.Form(album = album),
            onCancel = {},
            onNameChange = {},
            onDurationChange = {},
            onSuggestionSelected = {},
            onSubmit = {},
        )
    }
}
