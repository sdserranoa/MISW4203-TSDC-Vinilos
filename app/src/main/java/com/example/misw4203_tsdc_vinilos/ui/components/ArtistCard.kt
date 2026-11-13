package com.example.misw4203_tsdc_vinilos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.model.Performer
import com.example.misw4203_tsdc_vinilos.ui.theme.Amber
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/**
 * Tarjeta del listado de artistas (HU03) con imagen, nombre, tipo (Banda / Solista),
 * información contextual y álbum destacado si está disponible.
 */
@Composable
fun ArtistCard(
    artist: Performer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtistAvatar(
                imageUrl = artist.normalizedImage,
                name = artist.name,
                modifier = Modifier.size(60.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    ArtistTypeBadge(isBand = artist.isBand)
                }

                Spacer(modifier = Modifier.height(4.dp))

                val subtitleText = when {
                    artist.description.isNotBlank() -> artist.description
                    artist.albumCount > 0 -> pluralStringResource(R.plurals.collector_album_count, artist.albumCount, artist.albumCount)
                    else -> artist.performerType
                }

                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (artist.featuredAlbum != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_vinyl),
                            contentDescription = null,
                            tint = Amber,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.artist_featured_album, artist.featuredAlbum!!),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalIconButton(
                onClick = onClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = stringResource(R.string.performer_open_detail, artist.name),
                )
            }
        }
    }
}

@Composable
private fun ArtistAvatar(
    imageUrl: String,
    name: String,
    modifier: Modifier = Modifier,
) {
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.performer_avatar_description, name),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape),
        )
    } else {
        Surface(
            modifier = modifier,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_mic),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun ArtistTypeBadge(
    isBand: Boolean,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor, textRes) = if (isBand) {
        Triple(
            Amber.copy(alpha = 0.2f),
            Amber,
            R.string.artist_band_tag,
        )
    } else {
        Triple(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant,
            R.string.artist_solo_label,
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
    ) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ArtistCardPreview() {
    MISW4203TSDCVinilosTheme {
        ArtistCard(
            artist = Performer(
                id = 1,
                name = "Soda Stereo",
                image = "",
                description = "Rock en Español • 7 álbumes",
                creationDate = "1982-01-01",
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
