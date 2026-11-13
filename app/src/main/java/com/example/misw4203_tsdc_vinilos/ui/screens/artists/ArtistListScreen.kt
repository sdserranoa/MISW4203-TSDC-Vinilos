package com.example.misw4203_tsdc_vinilos.ui.screens.artists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.ui.components.ArtistCard
import com.example.misw4203_tsdc_vinilos.ui.components.LoadingMessage
import com.example.misw4203_tsdc_vinilos.ui.components.PaginationBar
import com.example.misw4203_tsdc_vinilos.ui.components.StateMessage
import com.example.misw4203_tsdc_vinilos.ui.components.VinilosTopBar
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** HU03 - Consultar listado de artistas. */
@Composable
fun ArtistListScreen(
    modifier: Modifier = Modifier,
    onArtistClick: (Int, Boolean) -> Unit = { _, _ -> },
    viewModel: ArtistListViewModel = viewModel(factory = ArtistListViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArtistListContent(
        uiState = uiState,
        onPreviousPage = viewModel::previousPage,
        onNextPage = viewModel::nextPage,
        onRetry = viewModel::loadArtists,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterTypeChange = viewModel::onFilterTypeChange,
        onArtistClick = onArtistClick,
        modifier = modifier,
    )
}

@Composable
fun ArtistListContent(
    uiState: ArtistListUiState,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onSearchQueryChange: (String) -> Unit = {},
    onFilterTypeChange: (ArtistFilterType) -> Unit = {},
    onArtistClick: (Int, Boolean) -> Unit = { _, _ -> },
) {
    Scaffold(
        modifier = modifier,
        topBar = { VinilosTopBar() },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            ArtistListUiState.Loading -> LoadingMessage(
                message = stringResource(R.string.artists_loading),
                modifier = contentModifier,
            )

            ArtistListUiState.Empty -> StateMessage(
                message = stringResource(R.string.artists_empty),
                modifier = contentModifier,
            )

            ArtistListUiState.Error -> StateMessage(
                message = stringResource(R.string.artists_error),
                modifier = contentModifier,
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
            )

            is ArtistListUiState.Success -> ArtistList(
                state = uiState,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                onSearchQueryChange = onSearchQueryChange,
                onFilterTypeChange = onFilterTypeChange,
                onArtistClick = onArtistClick,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun ArtistList(
    state: ArtistListUiState.Success,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterTypeChange: (ArtistFilterType) -> Unit,
    onArtistClick: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberSaveable(state.currentPage, saver = LazyListState.Saver) {
        LazyListState()
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "search", contentType = "search") {
            ArtistSearchField(query = state.searchQuery, onQueryChange = onSearchQueryChange)
        }
        item(key = "filters", contentType = "filters") {
            ArtistFilterChips(
                selectedFilter = state.filterType,
                onFilterSelected = onFilterTypeChange,
            )
        }
        item(key = "header", contentType = "header") {
            ArtistCountHeader(totalArtists = state.totalArtists)
        }
        if (state.artists.isEmpty()) {
            item(key = "no_results", contentType = "message") {
                Text(
                    text = stringResource(R.string.artist_search_empty, state.searchQuery),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                )
            }
        } else {
            items(
                items = state.artists,
                key = { if (it.isBand) "band_${it.id}" else "musician_${it.id}" },
                contentType = { "artist" },
            ) { artist ->
                ArtistCard(
                    artist = artist,
                    onClick = { onArtistClick(artist.id, artist.isBand) },
                )
            }
            item(key = "pagination", contentType = "pagination") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PaginationBar(
                        currentPage = state.currentPage,
                        totalPages = state.totalPages,
                        onPreviousPage = onPreviousPage,
                        onNextPage = onNextPage,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(R.string.artist_search_placeholder),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.artist_search_clear),
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun ArtistFilterChips(
    selectedFilter: ArtistFilterType,
    onFilterSelected: (ArtistFilterType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedFilter == ArtistFilterType.ALL,
            onClick = { onFilterSelected(ArtistFilterType.ALL) },
            label = { Text(stringResource(R.string.artist_filter_all)) },
            colors = FilterChipDefaults.filterChipColors(),
        )
        FilterChip(
            selected = selectedFilter == ArtistFilterType.BANDS,
            onClick = { onFilterSelected(ArtistFilterType.BANDS) },
            label = { Text(stringResource(R.string.artist_filter_bands)) },
            colors = FilterChipDefaults.filterChipColors(),
        )
        FilterChip(
            selected = selectedFilter == ArtistFilterType.MUSICIANS,
            onClick = { onFilterSelected(ArtistFilterType.MUSICIANS) },
            label = { Text(stringResource(R.string.artist_filter_solos)) },
            colors = FilterChipDefaults.filterChipColors(),
        )
    }
}

@Composable
private fun ArtistCountHeader(totalArtists: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mic),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = pluralStringResource(R.plurals.artists_header, totalArtists, totalArtists),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ArtistListContentPreview() {
    val performers = listOf(
        Performer(1, "Soda Stereo", "", "Rock en Español", creationDate = "1982-01-01"),
        Performer(2, "Joan Manuel Serrat", "", "Músico y poeta", birthDate = "1943-12-27"),
    )
    MISW4203TSDCVinilosTheme {
        ArtistListContent(
            uiState = ArtistListUiState.Success(
                artists = performers,
                totalArtists = 6,
                currentPage = 1,
                totalPages = 1,
            ),
            onPreviousPage = {},
            onNextPage = {},
            onRetry = {},
        )
    }
}
