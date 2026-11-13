package com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.CollectorDetail
import com.example.misw4203_tsdc_vinilos.model.Comment
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.model.initialsOf
import com.example.misw4203_tsdc_vinilos.ui.components.AlbumCard
import com.example.misw4203_tsdc_vinilos.ui.components.GlideImage
import com.example.misw4203_tsdc_vinilos.ui.components.InitialsAvatar
import com.example.misw4203_tsdc_vinilos.ui.components.LoadingMessage
import com.example.misw4203_tsdc_vinilos.ui.components.StateMessage
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** HU06 - Consultar la información detallada de un coleccionista. */
@Composable
fun CollectorDetailScreen(
    collectorId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAlbumClick: (Int) -> Unit = {},
    onPerformerClick: ((performerId: Int, isBand: Boolean) -> Unit)? = null,
    viewModel: CollectorDetailViewModel = viewModel(
        key = "collector_detail_$collectorId",
        factory = CollectorDetailViewModel.provideFactory(collectorId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectorDetailContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadCollector,
        modifier = modifier,
        onAlbumClick = onAlbumClick,
        onPerformerClick = onPerformerClick,
    )
}

/**
 * Contenido del detalle. [onPerformerClick] es opcional: si es nulo, los artistas favoritos se
 * muestran sin acción de navegación.
 */
@Composable
fun CollectorDetailContent(
    uiState: CollectorDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onAlbumClick: (Int) -> Unit = {},
    onPerformerClick: ((performerId: Int, isBand: Boolean) -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = { CollectorDetailTopBar(onBack = onBack) },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            CollectorDetailUiState.Loading -> LoadingMessage(
                message = stringResource(R.string.collector_detail_loading),
                modifier = contentModifier,
            )

            CollectorDetailUiState.Error -> StateMessage(
                message = stringResource(R.string.collector_detail_error),
                modifier = contentModifier,
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
            )

            is CollectorDetailUiState.Success -> CollectorDetailBody(
                collector = uiState.collector,
                onAlbumClick = onAlbumClick,
                onPerformerClick = onPerformerClick,
                modifier = contentModifier,
            )
        }
    }
}

/** Listados que se pueden consultar en el detalle del coleccionista. */
enum class CollectorDetailTab(@param:StringRes val titleRes: Int) {
    ALBUMS(R.string.collector_tab_albums),
    ARTISTS(R.string.collector_tab_artists),
    COMMENTS(R.string.collector_tab_comments),
}

@Composable
private fun CollectorDetailTopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
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
                        contentDescription = stringResource(R.string.back_to_collectors),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = stringResource(R.string.collector_detail_title),
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

/**
 * Cuerpo del detalle en una sola cuadrícula diferida: el perfil y las pestañas ocupan todo el
 * ancho y debajo se muestra el listado elegido. Solo se componen los elementos visibles.
 */
@Composable
private fun CollectorDetailBody(
    collector: CollectorDetail,
    onAlbumClick: (Int) -> Unit,
    onPerformerClick: ((performerId: Int, isBand: Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(CollectorDetailTab.ALBUMS) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        fullWidthItem(key = "profile", contentType = "profile") {
            CollectorProfileCard(collector = collector)
        }
        fullWidthItem(key = "tabs", contentType = "tabs") {
            CollectorDetailTabs(
                selectedTab = selectedTab,
                countOf = { tab ->
                    when (tab) {
                        CollectorDetailTab.ALBUMS -> collector.albums.size
                        CollectorDetailTab.ARTISTS -> collector.favoritePerformers.size
                        CollectorDetailTab.COMMENTS -> collector.comments.size
                    }
                },
                onTabSelected = { selectedTab = it },
            )
        }
        when (selectedTab) {
            CollectorDetailTab.ALBUMS -> albumItems(collector.albums, onAlbumClick)
            CollectorDetailTab.ARTISTS -> performerItems(collector.favoritePerformers, onPerformerClick)
            CollectorDetailTab.COMMENTS -> commentItems(collector.comments)
        }
    }
}

private fun LazyGridScope.fullWidthItem(
    key: String,
    contentType: String,
    content: @Composable LazyGridItemScope.() -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }, contentType = contentType, content = content)

private fun LazyGridScope.emptyListItem(@StringRes messageRes: Int) =
    fullWidthItem(key = "empty", contentType = "empty") {
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 12.dp),
        )
    }

/** Álbumes de la colección en cuadrícula: nombre, carátula, género y año (criterio 2). */
private fun LazyGridScope.albumItems(albums: List<Album>, onAlbumClick: (Int) -> Unit) {
    if (albums.isEmpty()) {
        emptyListItem(R.string.collector_albums_empty)
        return
    }
    items(items = albums, key = { "album_${it.id}" }, contentType = { "album" }) { album ->
        AlbumCard(album = album, onClick = { onAlbumClick(album.id) })
    }
}

/** Artistas favoritos con su nombre e imagen (criterio 2). */
private fun LazyGridScope.performerItems(
    performers: List<Performer>,
    onPerformerClick: ((performerId: Int, isBand: Boolean) -> Unit)?,
) {
    if (performers.isEmpty()) {
        emptyListItem(R.string.collector_artists_empty)
        return
    }
    items(
        items = performers,
        key = { "performer_${it.id}" },
        span = { GridItemSpan(maxLineSpan) },
        contentType = { "performer" },
    ) { performer ->
        FavoritePerformerItem(
            performer = performer,
            onClick = onPerformerClick?.let { { it(performer.id, performer.isBand) } },
        )
    }
}

/** Comentarios del coleccionista con su calificación (criterio 1). */
private fun LazyGridScope.commentItems(comments: List<Comment>) {
    if (comments.isEmpty()) {
        emptyListItem(R.string.collector_comments_empty)
        return
    }
    items(
        items = comments,
        key = { "comment_${it.id}" },
        span = { GridItemSpan(maxLineSpan) },
        contentType = { "comment" },
    ) { comment ->
        CollectorCommentItem(comment = comment)
    }
}

/** Encabezado con el avatar, el nombre, los totales y los datos de contacto del coleccionista. */
@Composable
private fun CollectorProfileCard(collector: CollectorDetail, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InitialsAvatar(
                initials = collector.initials,
                size = 96.dp,
                textStyle = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = collector.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CollectorStat(
                    value = collector.albums.size,
                    label = stringResource(R.string.collector_stat_albums),
                )
                CollectorStat(
                    value = collector.favoritePerformers.size,
                    label = stringResource(R.string.collector_stat_favorites),
                )
                CollectorStat(
                    value = collector.comments.size,
                    label = stringResource(R.string.collector_stat_comments),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (collector.telephone.isNotBlank()) {
                ContactPill(
                    iconRes = R.drawable.ic_phone,
                    iconDescription = stringResource(R.string.collector_phone_description),
                    text = collector.telephone,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (collector.email.isNotBlank()) {
                ContactPill(
                    iconRes = R.drawable.ic_mail,
                    iconDescription = stringResource(R.string.collector_email_description),
                    text = collector.email,
                )
            }
        }
    }
}

@Composable
private fun CollectorStat(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContactPill(
    @DrawableRes iconRes: Int,
    iconDescription: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = iconDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Pestañas con la cantidad de registros de cada listado, con el mismo estilo que las del detalle de
 * álbum. Cada pestaña toma el ancho de su texto para que ninguna etiqueta se corte, y la fila se
 * puede desplazar si la escala de fuente del dispositivo es muy grande.
 */
@Composable
private fun CollectorDetailTabs(
    selectedTab: CollectorDetailTab,
    countOf: (CollectorDetailTab) -> Int,
    onTabSelected: (CollectorDetailTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            CollectorDetailTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .selectable(selected = selected, role = Role.Tab, onClick = { onTabSelected(tab) })
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(tab.titleRes, countOf(tab)),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(if (selected) 36.dp else 0.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    }
}

/**
 * Artista favorito con su fotografía (o sus iniciales mientras carga o si no tiene) y su tipo.
 * Si [onClick] es nulo, la tarjeta no es interactiva.
 */
@Composable
private fun FavoritePerformerItem(
    performer: Performer,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, colors = colors) {
            FavoritePerformerRow(performer = performer, showChevron = true)
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = shape, colors = colors) {
            FavoritePerformerRow(performer = performer, showChevron = false)
        }
    }
}

@Composable
private fun FavoritePerformerRow(performer: Performer, showChevron: Boolean) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            val initials = remember(performer.name) { initialsOf(performer.name) }
            InitialsAvatar(initials = initials, size = 56.dp)
            if (performer.image.isNotBlank()) {
                // Tamaño fijo: Glide decodifica la imagen al tamaño indicado y no a la resolución original.
                GlideImage(
                    model = performer.image,
                    contentDescription = stringResource(R.string.performer_avatar_description, performer.name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    overrideWidthPx = 168,
                    overrideHeightPx = 168,
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = performer.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = performer.performerType,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (showChevron) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = stringResource(R.string.performer_open_detail, performer.name),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Comentario del coleccionista con su calificación en estrellas. */
@Composable
private fun CollectorCommentItem(comment: Comment, modifier: Modifier = Modifier) {
    val rating = comment.rating.coerceIn(0, MAX_RATING)
    val stars = remember(rating) { "★".repeat(rating) + "☆".repeat(MAX_RATING - rating) }
    val ratingDescription = stringResource(R.string.collector_rating_description, rating)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stars,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { contentDescription = ratingDescription },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = comment.description,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private const val MAX_RATING = 5

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CollectorDetailContentPreview() {
    MISW4203TSDCVinilosTheme {
        CollectorDetailContent(
            uiState = CollectorDetailUiState.Success(
                CollectorDetail(
                    id = 100,
                    name = "Manolo Bellon",
                    telephone = "3502457896",
                    email = "manollo@caracol.com.co",
                    albums = listOf(
                        Album(100, "Buscando América", "", "1984-08-01T00:00:00.000Z", genre = "Salsa"),
                        Album(101, "Poeta del pueblo", "", "1984-08-01T00:00:00.000Z", genre = "Salsa"),
                    ),
                    favoritePerformers = listOf(Performer(100, "Rubén Blades Bellido de Luna")),
                    comments = listOf(Comment(100, "The most relevant album of Ruben Blades", 5)),
                ),
            ),
            onBack = {},
            onRetry = {},
        )
    }
}
