package com.example.misw4203_tsdc_vinilos.ui.screens.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.model.Album
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.ui.components.AlbumCard
import com.example.misw4203_tsdc_vinilos.ui.components.LoadingMessage
import com.example.misw4203_tsdc_vinilos.ui.components.PaginationBar
import com.example.misw4203_tsdc_vinilos.ui.components.StateMessage
import com.example.misw4203_tsdc_vinilos.ui.components.VinilosTopBar
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** HU01 - Consultar catálogo de álbumes. */
@Composable
fun AlbumListScreen(
    modifier: Modifier = Modifier,
    viewModel: AlbumListViewModel = viewModel(factory = AlbumListViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumListContent(
        uiState = uiState,
        onPreviousPage = viewModel::previousPage,
        onNextPage = viewModel::nextPage,
        onRetry = viewModel::loadAlbums,
        modifier = modifier,
    )
}

@Composable
fun AlbumListContent(
    uiState: AlbumListUiState,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { VinilosTopBar() },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            AlbumListUiState.Loading -> LoadingMessage(
                message = stringResource(R.string.albums_loading),
                modifier = contentModifier,
            )

            AlbumListUiState.Empty -> StateMessage(
                message = stringResource(R.string.albums_empty),
                modifier = contentModifier,
            )

            AlbumListUiState.Error -> StateMessage(
                message = stringResource(R.string.albums_error),
                modifier = contentModifier,
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
            )

            is AlbumListUiState.Success -> AlbumGrid(
                state = uiState,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun AlbumGrid(
    state: AlbumListUiState.Success,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Estado de scroll indexado por página: cada página inicia en el tope y la posición
    // se preserva ante cambios de configuración.
    val gridState = rememberSaveable(state.currentPage, saver = LazyGridState.Saver) {
        LazyGridState()
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            AlbumCountHeader(totalAlbums = state.totalAlbums)
        }
        items(state.albums, key = { it.id }) { album ->
            AlbumCard(album = album)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
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

@Composable
private fun AlbumCountHeader(totalAlbums: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_graphic_eq),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = pluralStringResource(R.plurals.albums_header, totalAlbums, totalAlbums),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AlbumListContentPreview() {
    val albums = listOf(
        Album(1, "Buscando América", "", "1984-08-01T00:00:00.000Z", genre = "Salsa",
            performers = listOf(Performer(1, "Rubén Blades"))),
        Album(2, "Artaud", "", "1973-01-01T00:00:00.000Z", genre = "Rock",
            performers = listOf(Performer(2, "Pescado Rabioso"))),
        Album(3, "Siembra", "", "1978-08-01T00:00:00.000Z", genre = "Salsa",
            performers = listOf(Performer(3, "Willie Colón"), Performer(1, "Rubén Blades"))),
        Album(4, "Alturas de Macchu Picchu", "", "1981-01-01T00:00:00.000Z", genre = "Folk",
            performers = listOf(Performer(4, "Los Jaivas"))),
    )
    MISW4203TSDCVinilosTheme {
        AlbumListContent(
            uiState = AlbumListUiState.Success(
                albums = albums,
                totalAlbums = 48,
                currentPage = 1,
                totalPages = 5,
            ),
            onPreviousPage = {},
            onNextPage = {},
            onRetry = {},
        )
    }
}
