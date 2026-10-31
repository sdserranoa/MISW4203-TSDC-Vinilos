package com.example.misw4203_tsdc_vinilos.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.misw4203_tsdc_vinilos.R
import com.example.misw4203_tsdc_vinilos.ui.theme.MISW4203TSDCVinilosTheme

/** Secciones principales de la barra de navegación inferior. */
enum class VinilosTab(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    ALBUMS(R.string.nav_albums, R.drawable.ic_vinyl),
    ARTISTS(R.string.nav_artists, R.drawable.ic_mic),
    COLLECTORS(R.string.nav_collectors, R.drawable.ic_badge),
}

/** Componente reutilizable de barra de navegación inferior para la aplicación. */
@Composable
fun VinilosBottomBar(
    currentTab: VinilosTab,
    onTabSelected: (VinilosTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        VinilosTab.entries.forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter = painterResource(tab.iconRes),
                        contentDescription = stringResource(tab.labelRes),
                    )
                },
                label = {
                    Text(text = stringResource(tab.labelRes))
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun VinilosBottomBarPreview() {
    MISW4203TSDCVinilosTheme {
        VinilosBottomBar(
            currentTab = VinilosTab.ALBUMS,
            onTabSelected = {},
        )
    }
}
