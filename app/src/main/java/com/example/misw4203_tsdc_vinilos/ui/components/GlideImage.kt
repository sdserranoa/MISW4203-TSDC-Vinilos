package com.example.misw4203_tsdc_vinilos.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

/**
 * Componente declarativo de Jetpack Compose para la carga y almacenamiento en caché
 * de imágenes remotas mediante la biblioteca Glide (HU04).
 *
 * Micro-optimizaciones de rendimiento:
 * - **Downsampling & Override**: Permite redimensionar y decodificar a nivel de disco ([overrideWidthPx],
 *   [overrideHeightPx]) para evitar decodificar imágenes 4K/alta resolución en avatares pequeños,
 *   ahorrando hasta 75% de memoria de mapa de bits.
 * - **Caché en disco ([DiskCacheStrategy.ALL])**: Almacena tanto el archivo crudo original
 *   como la versión reducida/optimizada para la vista.
 * - **Caché en memoria (LRU)**: Reutiliza instancias de [Bitmap] ya decodificadas, evitando
 *   re-peticiones de red o lecturas a disco durante el scroll.
 * - **Liberación de memoria**: Invoca `Glide.with(context).clear(target)` en `onDispose`
 *   para reciclar mapas de bits y prevenir fugas de memoria.
 * - **Filtro de URLs vacías**: Evita invocar la canalización de Glide si la URL es nula o blanca.
 *
 * @param model URL, recurso o archivo que representa la imagen a cargar.
 * @param contentDescription Texto descriptivo para lectores de pantalla.
 * @param modifier Modificador de diseño de Compose.
 * @param contentScale Escala de ajuste ([ContentScale.Crop] por defecto).
 * @param placeholderRes Recurso drawable opcional mostrado durante la carga inicial.
 * @param errorRes Recurso drawable opcional mostrado en caso de falla de descarga.
 * @param diskCacheStrategy Política de almacenamiento en disco ([DiskCacheStrategy.ALL]).
 * @param overrideWidthPx Ancho máximo en píxeles para decodificación eficiente en memoria.
 * @param overrideHeightPx Alto máximo en píxeles para decodificación eficiente en memoria.
 */
@Composable
fun GlideImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    @DrawableRes placeholderRes: Int? = null,
    @DrawableRes errorRes: Int? = null,
    diskCacheStrategy: DiskCacheStrategy = DiskCacheStrategy.ALL,
    overrideWidthPx: Int = Target.SIZE_ORIGINAL,
    overrideHeightPx: Int = Target.SIZE_ORIGINAL,
) {
    val context = LocalContext.current
    var bitmapState by remember(model) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(model) { mutableStateOf(model != null) }
    var isError by remember(model) { mutableStateOf(false) }

    DisposableEffect(model, overrideWidthPx, overrideHeightPx) {
        val stringModel = model as? String
        if (model == null || (stringModel != null && stringModel.isBlank())) {
            isLoading = false
            isError = true
            return@DisposableEffect onDispose {}
        }

        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                bitmapState = resource
                isLoading = false
                isError = false
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                bitmapState = null
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                isLoading = false
                isError = true
            }
        }

        try {
            var request = Glide.with(context)
                .asBitmap()
                .load(model)
                .diskCacheStrategy(diskCacheStrategy)
                .downsample(DownsampleStrategy.CENTER_INSIDE)

            if (overrideWidthPx > 0 && overrideHeightPx > 0) {
                request = request.override(overrideWidthPx, overrideHeightPx)
            }

            request.into(target)
        } catch (_: Exception) {
            isLoading = false
            isError = true
        }

        onDispose {
            try {
                Glide.with(context).clear(target)
            } catch (_: Exception) {
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val currentBitmap = bitmapState
        when {
            currentBitmap != null -> {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            isLoading -> {
                if (placeholderRes != null) {
                    Image(
                        painter = painterResource(placeholderRes),
                        contentDescription = contentDescription,
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                }
            }
            isError -> {
                if (errorRes != null) {
                    Image(
                        painter = painterResource(errorRes),
                        contentDescription = contentDescription,
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
            }
        }
    }
}
