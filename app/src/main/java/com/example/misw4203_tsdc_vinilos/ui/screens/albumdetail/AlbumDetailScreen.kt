package com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.Track
import com.example.misw4203_tsdc_vinilos.ui.components.LoadingMessage
import com.example.misw4203_tsdc_vinilos.ui.components.StateMessage
import com.example.misw4203_tsdc_vinilos.ui.theme.Amber
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** HU02 - Consultar la información detallada de un álbum. */
@Composable
fun AlbumDetailScreen(
    albumId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = viewModel(
        key = "album_detail_$albumId",
        factory = AlbumDetailViewModel.provideFactory(albumId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumDetailContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadAlbum,
        modifier = modifier,
    )
}

@Composable
fun AlbumDetailContent(
    uiState: AlbumDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AlbumDetailTopBar(
                onBack = onBack,
                onFavoriteClick = onFavoriteClick,
                onShareClick = onShareClick,
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            AlbumDetailUiState.Loading -> LoadingMessage(
                message = stringResource(R.string.album_detail_loading),
                modifier = contentModifier,
            )

            AlbumDetailUiState.Error -> StateMessage(
                message = stringResource(R.string.album_detail_error),
                modifier = contentModifier,
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
            )

            is AlbumDetailUiState.Success -> AlbumDetailBody(
                album = uiState.album,
                modifier = contentModifier,
            )
        }
    }
}

/** Barra de navegación superior según el diseño del mockup. */
@Composable
fun AlbumDetailTopBar(
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
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
                        contentDescription = stringResource(R.string.back_to_catalog),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = stringResource(R.string.album_detail_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .semantics { heading() },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        }
    }
}

/** Pestañas disponibles en la vista de detalle. */
enum class AlbumDetailTab(val titleRes: Int) {
    TRACKS(R.string.tab_tracks),
    PERFORMERS(R.string.tab_performers),
    COMMENTS(R.string.tab_comments),
}

/** Cuerpo con la información base del álbum y las secciones de contenido. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlbumDetailBody(
    album: Album,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var selectedTab by rememberSaveable { mutableStateOf(AlbumDetailTab.TRACKS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hero visual: mockup de disco de vinilo asomándose tras la carátula
        VinylAlbumHero(
            album = album,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // Nombre del álbum
        Text(
            text = album.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Fila de etiquetas (Chips) centrada
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val performers = album.performers.joinToString { it.name }
            if (performers.isNotBlank()) {
                MetadataBadge(
                    text = performers,
                    containerColor = Amber.copy(alpha = 0.18f),
                    contentColor = Amber,
                    borderColor = Amber.copy(alpha = 0.45f),
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            MetadataBadge(
                text = album.genre,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                borderColor = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            MetadataBadge(
                text = album.releaseYear,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                borderColor = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            if (album.recordLabel.isNotBlank()) {
                MetadataBadge(
                    text = album.recordLabel,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción expandible
        if (album.description.isNotBlank()) {
            AlbumDescription(
                description = album.description,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selector segmentado de pestañas según mockup
        AlbumDetailTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido de la sección activa
        when (selectedTab) {
            AlbumDetailTab.TRACKS -> AlbumTracklistSection(
                album = album,
                modifier = Modifier.fillMaxWidth(),
            )
            AlbumDetailTab.PERFORMERS -> AlbumPerformersSection(
                album = album,
                modifier = Modifier.fillMaxWidth(),
            )
            AlbumDetailTab.COMMENTS -> AlbumCommentsSection(
                album = album,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Pestañas segmentadas con acento dorado y línea activa. */
@Composable
fun AlbumDetailTabs(
    selectedTab: AlbumDetailTab,
    onTabSelected: (AlbumDetailTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                AlbumDetailTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    Column(
                        modifier = Modifier
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(tab.titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Amber else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .height(2.5.dp)
                                .width(if (isSelected) 36.dp else 0.dp)
                                .background(
                                    color = if (isSelected) Amber else Color.Transparent,
                                    shape = RoundedCornerShape(2.dp),
                                )
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        }
    }
}

/** Listado 1: Pistas / Canciones del álbum (lista limpia sin ecualizador). */
@Composable
fun AlbumTracklistSection(
    album: Album,
    modifier: Modifier = Modifier,
    onSamplePlay: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Encabezado de la lista de canciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.tracks_section_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                    )
                    val tracksCount = album.tracks.size
                    val subtitle = if (tracksCount > 0) {
                        val totalSeconds = album.tracks.sumOf { track ->
                            val parts = track.duration.split(":")
                            if (parts.size == 2) {
                                (parts[0].trim().toIntOrNull() ?: 0) * 60 + (parts[1].trim().toIntOrNull() ?: 0)
                            } else 0
                        }
                        val min = totalSeconds / 60
                        val sec = totalSeconds % 60
                        if (totalSeconds > 0) {
                            "$tracksCount canciones • $min:${sec.toString().padStart(2, '0')} min"
                        } else {
                            "$tracksCount canciones"
                        }
                    } else {
                        "0 canciones"
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (album.tracks.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Amber.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Amber.copy(alpha = 0.35f)),
                        modifier = Modifier.clickable { onSamplePlay() },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Canvas(modifier = Modifier.size(8.dp)) {
                                val path = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, size.height / 2f)
                                    lineTo(0f, size.height)
                                    close()
                                }
                                drawPath(path, color = Amber)
                            }
                            Text(
                                text = stringResource(R.string.tracks_sample_button),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Amber,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            if (album.tracks.isEmpty()) {
                Text(
                    text = stringResource(R.string.tracks_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                )
            } else {
                album.tracks.forEachIndexed { index, track ->
                    TrackListItem(
                        trackNumber = index + 1,
                        track = track,
                        performerName = album.performers.firstOrNull()?.name ?: "",
                    )
                    if (index < album.tracks.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Elemento de pista individual: número de pista, título, artista y duración. */
@Composable
fun TrackListItem(
    trackNumber: Int,
    track: Track,
    performerName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Número de pista
        Text(
            text = "$trackNumber",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Título de la pista y artista / compositor
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (performerName.isNotBlank()) {
                Text(
                    text = performerName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Duración en fuente monoespaciada
        Text(
            text = track.duration.ifBlank { "--:--" },
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Listado 2: Intérpretes y Músicos asociados al álbum. */
@Composable
fun AlbumPerformersSection(
    album: Album,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.performers_section_title).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
                if (album.performers.isNotEmpty()) {
                    Text(
                        text = "${album.performers.size} ${if (album.performers.size == 1) "artista" else "artistas"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Amber,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            if (album.performers.isEmpty()) {
                Text(
                    text = stringResource(R.string.performers_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                )
            } else {
                album.performers.forEachIndexed { index, performer ->
                    PerformerListItem(
                        performer = performer,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (index < album.performers.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Tarjeta individual de intérprete con avatar circular, nombre, tipo y biografía. */
@Composable
fun PerformerListItem(
    performer: Performer,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar circular con imagen o iniciales
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = BorderStroke(1.5.dp, Amber.copy(alpha = 0.6f)),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                // Iniciales de respaldo siempre visibles de fondo
                val initials = performer.name
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .joinToString("")
                    .ifBlank { "AR" }
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Amber,
                )

                if (performer.normalizedImage.isNotBlank()) {
                    AsyncImage(
                        model = performer.normalizedImage,
                        contentDescription = stringResource(R.string.performer_avatar_description, performer.name),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Datos del intérprete
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = performer.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = performer.performerType,
                style = MaterialTheme.typography.labelSmall,
                color = Amber,
                fontWeight = FontWeight.Medium,
            )
            if (performer.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = performer.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Listado 3: Reseñas y Comentarios de coleccionistas. */
@Composable
fun AlbumCommentsSection(
    album: Album,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Cabecera con título y promedio de estrellas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.comments_section_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                    )
                    if (album.comments.isNotEmpty()) {
                        val avgRating = album.comments.map { it.rating }.average()
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Amber.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, Amber.copy(alpha = 0.35f)),
                        ) {
                            Text(
                                text = String.format("%.1f / 5.0", avgRating),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Amber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                if (album.comments.isNotEmpty()) {
                    Text(
                        text = "${album.comments.size} reseñas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            if (album.comments.isEmpty()) {
                Text(
                    text = stringResource(R.string.comments_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                )
            } else {
                album.comments.forEachIndexed { index, comment ->
                    CommentListItem(
                        comment = comment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                    )
                    if (index < album.comments.lastIndex) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

/** Tarjeta individual de reseña con autor, badge de verificado, estrellas y texto. */
@Composable
fun CommentListItem(
    comment: Comment,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Avatar simple del coleccionista
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = Amber.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Amber.copy(alpha = 0.4f)),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            val collectorInitial = (comment.collector?.name?.firstOrNull() ?: 'C').uppercaseChar()
                            Text(
                                text = "$collectorInitial",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Amber,
                            )
                        }
                    }

                    Column {
                        val authorName = comment.collector?.name?.ifBlank { null }
                            ?: stringResource(R.string.collector_default_name)
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "✓ " + stringResource(R.string.collector_verified),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Amber,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                // Estrellas doradas según rating (1 a 5)
                val starsText = buildString {
                    val clampedRating = comment.rating.coerceIn(1, 5)
                    repeat(clampedRating) { append("★") }
                    repeat(5 - clampedRating) { append("☆") }
                }
                Text(
                    text = starsText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Amber,
                    letterSpacing = 1.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${comment.description}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
        }
    }
}

/** Mockup visual del disco de vinilo y la carátula. */
@Composable
fun VinylAlbumHero(
    album: Album,
    modifier: Modifier = Modifier,
) {
    val coverPlaceholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(width = 300.dp, height = 240.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Disco de vinilo que sobresale a la derecha
            VinylDiscGraphic(
                recordLabel = album.recordLabel,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 16.dp)
                    .size(210.dp),
            )

            // Carátula tipo funda de cartón (sleeve) al frente
            Card(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.CenterStart),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = album.cover,
                        contentDescription = stringResource(R.string.album_cover_description, album.name),
                        contentScale = ContentScale.Crop,
                        placeholder = coverPlaceholder,
                        error = coverPlaceholder,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Distintivo de edición para audiófilos
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xDD0D0D0D),
                        border = BorderStroke(0.6.dp, Amber.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopStart),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Amber, CircleShape),
                            )
                            Text(
                                text = stringResource(R.string.audiophile_badge),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = Amber,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Gráfico estilizado del disco de vinilo con surcos concéntricos y galleta central. */
@Composable
fun VinylDiscGraphic(
    recordLabel: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF2C2C2C), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Surcos del vinilo dibujados con Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val maxRadius = size.minDimension / 2
            val ringCount = 12
            for (i in 3 until ringCount) {
                val ringRadius = maxRadius * (i.toFloat() / ringCount)
                drawCircle(
                    color = Color(0xFF262626),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = 1f),
                )
            }
        }

        // Galleta central (etiqueta del disco)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF59E0B),
                            Color(0xFFB45309),
                            Color(0xFF78350F),
                        )
                    )
                )
                .border(2.dp, Color(0xFF121212), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = recordLabel.ifBlank { "VINILOS" }.take(9).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    ),
                    color = Color(0xFF121212),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Agujero central
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF121212), CircleShape)
                        .border(0.5.dp, Color(0xFF78350F), CircleShape),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "33⅓ RPM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color(0xFF121212),
                )
            }
        }
    }
}

/** Chip de información contextual (Género, Año, Sello o Intérprete). */
@Composable
private fun MetadataBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

/** Descripción con opción de expandir/colapsar si es extensa. */
@Composable
private fun AlbumDescription(
    description: String,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isLongDescription = description.length > 140

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(14.dp)
            .animateContentSize(),
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
            maxLines = if (isExpanded || !isLongDescription) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
        )

        if (isLongDescription) {
            Text(
                text = stringResource(if (isExpanded) R.string.read_less else R.string.read_more),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Amber,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { isExpanded = !isExpanded },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AlbumDetailContentPreview() {
    val album = Album(
        id = 1,
        name = "Abbey Road",
        cover = "",
        releaseDate = "1969-09-26T00:00:00.000Z",
        genre = "Rock Clásico",
        recordLabel = "Apple Records",
        description = "El undécimo álbum de estudio de la legendaria banda británica The Beatles.",
        performers = listOf(Performer(1, "The Beatles", description = "Legendaria banda de rock")),
        tracks = listOf(
            Track(1, "Come Together", "04:20"),
            Track(2, "Something", "03:03"),
        ),
        comments = listOf(
            Comment(1, "Una obra maestra sin precedentes", 5),
        ),
    )
    MISW4203TSDCVinilosTheme {
        AlbumDetailContent(
            uiState = AlbumDetailUiState.Success(album),
            onBack = {},
            onRetry = {},
        )
    }
}
