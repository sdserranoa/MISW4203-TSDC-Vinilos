package com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.model.Collector
import com.example.misw4203_tsdc_vinilos.model.CollectorAlbum
import com.example.misw4203_tsdc_vinilos.model.initialsOf
import com.example.misw4203_tsdc_vinilos.ui.components.InitialsAvatar
import com.example.misw4203_tsdc_vinilos.ui.components.LoadingMessage
import com.example.misw4203_tsdc_vinilos.ui.components.StateMessage
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** Detalle de un coleccionista, al que se llega desde el listado de coleccionistas (HU05). */
@Composable
fun CollectorDetailScreen(
    collectorId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
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
    )
}

@Composable
fun CollectorDetailContent(
    uiState: CollectorDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
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
                modifier = contentModifier,
            )
        }
    }
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

@Composable
private fun CollectorDetailBody(collector: Collector, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "profile", contentType = "profile") {
            CollectorProfileCard(collector = collector)
        }
    }
}

/** Encabezado con el avatar, el nombre, los totales y los datos de contacto del coleccionista. */
@Composable
private fun CollectorProfileCard(collector: Collector, modifier: Modifier = Modifier) {
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
            val initials = remember(collector.name) { initialsOf(collector.name) }
            InitialsAvatar(
                initials = initials,
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
                    value = collector.collectorAlbums.size,
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CollectorDetailContentPreview() {
    MISW4203TSDCVinilosTheme {
        CollectorDetailContent(
            uiState = CollectorDetailUiState.Success(
                Collector(
                    id = 100,
                    name = "Manolo Bellon",
                    telephone = "3502457896",
                    email = "manollo@caracol.com.co",
                    collectorAlbums = listOf(CollectorAlbum(id = 100, price = 35.0, status = "Active")),
                ),
            ),
            onBack = {},
            onRetry = {},
        )
    }
}
