package com.example.misw4203_tsdc_vinilos.ui.screens.collectors

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
import com.example.misw4203_tsdc_vinilos.model.CollectorSummary
import com.example.misw4203_tsdc_vinilos.ui.components.CollectorCard
import com.example.misw4203_tsdc_vinilos.ui.components.LoadingMessage
import com.example.misw4203_tsdc_vinilos.ui.components.PaginationBar
import com.example.misw4203_tsdc_vinilos.ui.components.StateMessage
import com.example.misw4203_tsdc_vinilos.ui.components.VinilosTopBar
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** HU05 - Consultar listado de coleccionistas. */
@Composable
fun CollectorListScreen(
    modifier: Modifier = Modifier,
    onCollectorClick: (Int) -> Unit = {},
    viewModel: CollectorListViewModel = viewModel(factory = CollectorListViewModel.Factory),
) {
    // Solo se recolecta el estado mientras la pantalla está al menos en STARTED.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectorListContent(
        uiState = uiState,
        onPreviousPage = viewModel::previousPage,
        onNextPage = viewModel::nextPage,
        onRetry = viewModel::loadCollectors,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCollectorClick = onCollectorClick,
        modifier = modifier,
    )
}

@Composable
fun CollectorListContent(
    uiState: CollectorListUiState,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onSearchQueryChange: (String) -> Unit = {},
    onCollectorClick: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = { VinilosTopBar() },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            CollectorListUiState.Loading -> LoadingMessage(
                message = stringResource(R.string.collectors_loading),
                modifier = contentModifier,
            )

            CollectorListUiState.Empty -> StateMessage(
                message = stringResource(R.string.collectors_empty),
                modifier = contentModifier,
            )

            CollectorListUiState.Error -> StateMessage(
                message = stringResource(R.string.collectors_error),
                modifier = contentModifier,
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
            )

            is CollectorListUiState.Success -> CollectorList(
                state = uiState,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                onSearchQueryChange = onSearchQueryChange,
                onCollectorClick = onCollectorClick,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun CollectorList(
    state: CollectorListUiState.Success,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCollectorClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Estado de scroll indexado por página: cada página inicia en el tope y la posición
    // se conserva ante cambios de configuración.
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
            CollectorSearchField(query = state.searchQuery, onQueryChange = onSearchQueryChange)
        }
        item(key = "header", contentType = "header") {
            CollectorCountHeader(totalCollectors = state.totalCollectors)
        }
        if (state.collectors.isEmpty()) {
            item(key = "no_results", contentType = "message") {
                Text(
                    text = stringResource(R.string.collector_search_empty, state.searchQuery),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                )
            }
        } else {
            // La clave estable y el tipo de contenido permiten a Compose reutilizar los nodos
            // de las tarjetas al paginar o filtrar, en lugar de crearlos de nuevo.
            items(
                items = state.collectors,
                key = { it.id },
                contentType = { "collector" },
            ) { collector ->
                CollectorCard(
                    collector = collector,
                    onClick = { onCollectorClick(collector.id) },
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
private fun CollectorSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            // Una sola línea: con escalas de fuente grandes el texto no agranda el campo.
            Text(
                text = stringResource(R.string.collector_search_placeholder),
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
                        contentDescription = stringResource(R.string.collector_search_clear),
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun CollectorCountHeader(totalCollectors: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_badge),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = pluralStringResource(R.plurals.collectors_header, totalCollectors, totalCollectors),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CollectorListContentPreview() {
    val collectors = listOf(
        CollectorSummary(100, "Manolo Bellon", "manollo@caracol.com.co", 1),
        CollectorSummary(101, "Jaime Monsalve", "jmonsalve@rtvc.com.co", 1),
        CollectorSummary(102, "Camila Restrepo", "c.restrepo@musicbox.co", 38),
    )
    MISW4203TSDCVinilosTheme {
        CollectorListContent(
            uiState = CollectorListUiState.Success(
                collectors = collectors,
                totalCollectors = 42,
                currentPage = 1,
                totalPages = 5,
            ),
            onPreviousPage = {},
            onNextPage = {},
            onRetry = {},
        )
    }
}
