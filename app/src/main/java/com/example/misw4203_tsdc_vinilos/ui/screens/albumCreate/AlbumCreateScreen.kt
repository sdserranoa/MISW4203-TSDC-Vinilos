package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.ui.components.LoadingMessage
import com.example.misw4203_tsdc_vinilos.ui.components.StateMessage
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme
import java.time.Instant
import java.time.ZoneId

/** HU07 - Crear un álbum. */
@Composable
fun AlbumCreateScreen(
    modifier: Modifier = Modifier,
    viewModel: AlbumCreateViewModel = viewModel(factory = AlbumCreateViewModel.Factory),
    onAlbumSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumCreateContent(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onAlbumSubmit = onAlbumSubmit,
        onRetry = viewModel::newAlbum,
        onNameChange = viewModel::onNameChange,
        onCoverChange = viewModel::onCoverChange,
        onReleaseDateChange = viewModel::onReleaseDateChange,
        onGenreChange = viewModel::onGenreChange,
        onRecordLabelChange = viewModel::onRecordLabelChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSaveAlbum = { viewModel.saveAlbum { onAlbumSubmit() } },
        onDismissSnackbar = viewModel::dismissSnackbar,
    )
}

@Composable
fun AlbumCreateContent(
    modifier: Modifier = Modifier,
    uiState: AlbumCreateUiState,
    onBack: () -> Unit,
    onAlbumSubmit: () -> Unit,
    onRetry: () -> Unit,
    onNameChange: (String) -> Unit = {},
    onCoverChange: (String) -> Unit = {},
    onReleaseDateChange: (String) -> Unit = {},
    onGenreChange: (String) -> Unit = {},
    onRecordLabelChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onSaveAlbum: () -> Unit = {},
    onDismissSnackbar: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AlbumCreateTopBar(
                onBack = onBack,
            )
        },
        bottomBar = {
            if (uiState is AlbumCreateUiState.Success) {
                StickyBottomActionBar(
                    onSave = onSaveAlbum,
                    onCancel = onBack,
                )
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            AlbumCreateUiState.Loading -> LoadingMessage(
                message = stringResource(R.string.album_create_loading),
                modifier = contentModifier,
            )

            AlbumCreateUiState.Error -> StateMessage(
                message = stringResource(R.string.album_create_error),
                modifier = contentModifier,
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
            )

            is AlbumCreateUiState.Success -> Box(
                modifier = contentModifier.fillMaxSize(),
            ) {
                AlbumCreateBody(
                    album = uiState.album,
                    onNameChange = onNameChange,
                    onCoverChange = onCoverChange,
                    onReleaseDateChange = onReleaseDateChange,
                    onGenreChange = onGenreChange,
                    onRecordLabelChange = onRecordLabelChange,
                    onDescriptionChange = onDescriptionChange,
                )

                // Floating Success Snackbar
                AnimatedVisibility(
                    visible = uiState.showSnackbar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    SuccessSnackbar(
                        onViewAlbum = onAlbumSubmit,
                        onDismiss = onDismissSnackbar,
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumCreateTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.album_create_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = stringResource(R.string.album_create_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .semantics { heading() },
                )

                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.album_create_more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumCreateBody(
    album: Album,
    onNameChange: (String) -> Unit,
    onCoverChange: (String) -> Unit,
    onReleaseDateChange: (String) -> Unit,
    onGenreChange: (String) -> Unit,
    onRecordLabelChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val formatted = String.format(
                                "%04d-%02d-%02d",
                                localDate.year,
                                localDate.monthValue,
                                localDate.dayOfMonth
                            )
                            onReleaseDateChange(formatted)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Status & Guidelines Banner
        StatusGuidelinesBanner()

        // Campo 1: Nombre del Álbum
        FormNameField(
            name = album.name,
            onNameChange = onNameChange,
        )

        // Campo 2: URL de Carátula + Preview Activo
        FormCoverUrlField(
            coverUrl = album.cover,
            albumName = album.name,
            releaseYear = album.releaseYear,
            onCoverChange = onCoverChange,
        )

        // Campo 3: Fecha de Lanzamiento
        FormReleaseDateField(
            releaseDate = album.releaseDate,
            onOpenDatePicker = { showDatePicker = true },
        )

        // Campo 4: Selector de Género
        FormGenreField(
            selectedGenre = album.genre,
            onGenreChange = onGenreChange,
        )

        // Campo 5: Selector de Sello Discográfico
        FormRecordLabelField(
            selectedRecordLabel = album.recordLabel,
            onRecordLabelChange = onRecordLabelChange,
        )

        // Campo 6: Descripción del Álbum
        FormDescriptionField(
            description = album.description,
            onDescriptionChange = onDescriptionChange,
        )

        // Collector Extra Info Pill (Bento Accent)
        CollectorExtraInfoPill()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatusGuidelinesBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_vinyl),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.album_create_banner_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp,
                    )
                    Text(
                        text = stringResource(R.string.album_create_banner_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.album_create_badge_hu),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun FormNameField(
    name: String,
    onNameChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FormLabel(
            label = stringResource(R.string.album_create_name_label),
            isRequired = true,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_radio_button_checked),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (name.isEmpty()) {
                        Text(
                            text = stringResource(R.string.album_create_name_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    BasicTextField(
                        value = name,
                        onValueChange = onNameChange,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.album_create_name_max_chars),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "${name.length}/100",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun FormCoverUrlField(
    coverUrl: String,
    albumName: String,
    releaseYear: String,
    onCoverChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FormLabel(
            label = stringResource(R.string.album_create_cover_label),
            isRequired = true,
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_link),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (coverUrl.isEmpty()) {
                        Text(
                            text = stringResource(R.string.album_create_cover_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    BasicTextField(
                        value = coverUrl,
                        onValueChange = onCoverChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                TextButton(onClick = { }) {
                    Text(
                        text = stringResource(R.string.album_create_cover_test),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Preview Box (MD3 Card Layout)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Vinyl Record Peek Visual
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(8.dp)),
                ) {
                    // Vinyl disc peeking from behind
                    Box(
                        modifier = Modifier
                            .offset(x = 16.dp, y = 4.dp)
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0E0E0E)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0E0E0E)),
                            )
                        }
                    }

                    // Cover image foreground
                    AsyncImage(
                        model = coverUrl.ifBlank { "https://images.vinilos.app/covers/abbey-road.jpg" },
                        contentDescription = stringResource(R.string.album_cover_description, albumName),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        )
                        Text(
                            text = stringResource(R.string.album_create_preview_active),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val displayName = albumName.ifBlank { "Abbey Road" }
                    val displayYear = releaseYear.ifBlank { "1969" }
                    Text(
                        text = "$displayName ($displayYear)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = stringResource(R.string.album_create_preview_dims),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.clickable { },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_photo_library),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = stringResource(R.string.album_create_cover_change),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormReleaseDateField(
    releaseDate: String,
    onOpenDatePicker: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FormLabel(
            label = stringResource(R.string.album_create_release_label),
            isRequired = true,
        )
        Spacer(modifier = Modifier.height(6.dp))

        val displayDate = remember(releaseDate) {
            if (releaseDate.contains("-") && releaseDate.length >= 10) {
                val parts = releaseDate.take(10).split("-")
                if (parts.size == 3) "${parts[2]} / ${parts[1]} / ${parts[0]}" else releaseDate
            } else if (releaseDate.isBlank()) {
                "26 / 09 / 1969"
            } else {
                releaseDate
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDatePicker() },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_today),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_edit_calendar),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = stringResource(R.string.album_create_release_format),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
        )
    }
}

@Composable
private fun FormGenreField(
    selectedGenre: String,
    onGenreChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val genres = remember {
        listOf("Rock Clásico", "Rock", "Jazz", "Salsa", "Folk", "Indie", "Electrónica")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        FormLabel(
            label = stringResource(R.string.album_create_genre_label),
            isRequired = true,
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_queue_music),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedGenre.ifBlank { "Rock Clásico" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_drop_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                genres.forEach { genre ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = genre,
                                color = if (genre == selectedGenre) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            onGenreChange(genre)
                            expanded = false
                        },
                    )
                }
            }
        }

        // Quick Genre Badges (Moments of Delight)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val quickChips = listOf("Rock Clásico", "Jazz", "Salsa Brava", "Folk Acústico")
            quickChips.forEach { chipText ->
                val isSelected = selectedGenre.equals(chipText, ignoreCase = true) ||
                        (selectedGenre == "Rock Clásico" && chipText == "Rock Clásico") ||
                        (selectedGenre == "Jazz" && chipText == "Jazz") ||
                        (selectedGenre == "Salsa" && chipText == "Salsa Brava") ||
                        (selectedGenre == "Folk" && chipText == "Folk Acústico")

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.clickable {
                        val mapped = when (chipText) {
                            "Salsa Brava" -> "Salsa"
                            "Folk Acústico" -> "Folk"
                            else -> chipText
                        }
                        onGenreChange(mapped)
                    },
                ) {
                    Text(
                        text = if (isSelected) "✓ $chipText" else chipText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FormRecordLabelField(
    selectedRecordLabel: String,
    onRecordLabelChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val recordLabels = remember {
        listOf("Apple Records / EMI", "Sony Music", "Columbia Records", "Fania All-Stars", "Atlantic Records", "Blue Note")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        FormLabel(
            label = stringResource(R.string.album_create_label_label),
            isRequired = true,
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_corporate_fare),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedRecordLabel.ifBlank { "Apple Records / EMI" },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_drop_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                recordLabels.forEach { label ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                color = if (label == selectedRecordLabel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            onRecordLabelChange(label)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FormDescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FormLabel(
            label = stringResource(R.string.album_create_desc_label),
            isRequired = true,
        )
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                if (description.isEmpty()) {
                    Text(
                        text = stringResource(R.string.album_create_desc_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                BasicTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.album_create_desc_notes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "${description.length}/500",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun CollectorExtraInfoPill() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_hearing),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.album_create_bento_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.album_create_bento_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun FormLabel(
    label: String,
    isRequired: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (isRequired) {
            Text(
                text = " *",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun SuccessSnackbar(
    onViewAlbum: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.album_create_snackbar_success),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onViewAlbum) {
                    Text(
                        text = stringResource(R.string.album_create_snackbar_view),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun StickyBottomActionBar(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onSave,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.album_create_save_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            TextButton(
                onClick = onCancel,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
            ) {
                Text(
                    text = stringResource(R.string.album_create_cancel_button),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Formulario de Creación", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun AlbumCreateContentPreview() {
    val sampleAlbum = Album(
        id = 0,
        name = "Abbey Road",
        cover = "https://images.vinilos.app/covers/abbey-road.jpg",
        releaseDate = "1969-09-26",
        description = "Undécimo álbum de estudio publicado por la banda británica The Beatles en 1969. Destacado por su célebre medley en la cara B y producido meticulosamente por George Martin en los estudios londinenses de Abbey Road.",
        genre = "Rock Clásico",
        recordLabel = "Apple Records / EMI",
    )
    MISW4203TSDCVinilosTheme {
        AlbumCreateContent(
            uiState = AlbumCreateUiState.Success(album = sampleAlbum),
            onBack = {},
            onAlbumSubmit = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Con Snackbar de Éxito", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun AlbumCreateContentSnackbarPreview() {
    val sampleAlbum = Album(
        id = 0,
        name = "Abbey Road",
        cover = "https://images.vinilos.app/covers/abbey-road.jpg",
        releaseDate = "1969-09-26",
        description = "Undécimo álbum de estudio publicado por la banda británica The Beatles en 1969.",
        genre = "Rock Clásico",
        recordLabel = "Apple Records / EMI",
    )
    MISW4203TSDCVinilosTheme {
        AlbumCreateContent(
            uiState = AlbumCreateUiState.Success(
                album = sampleAlbum,
                showSnackbar = true,
            ),
            onBack = {},
            onAlbumSubmit = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Cargando", showBackground = true, backgroundColor = 0xFF131313)
@Composable
private fun AlbumCreateContentLoadingPreview() {
    MISW4203TSDCVinilosTheme {
        AlbumCreateContent(
            uiState = AlbumCreateUiState.Loading,
            onBack = {},
            onAlbumSubmit = {},
            onRetry = {},
        )
    }
}
